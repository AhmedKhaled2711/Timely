package com.lee.timely.features.group.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lee.timely.model.Repository
import com.lee.timely.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import com.lee.timely.domain.AcademicYearPayment

data class GroupDetailsUiState(
    val groupId: Int = 0,
    val searchQuery: String = "",
    val selectedMonth: Int? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdatingPayment: Boolean = false, // New state for payment updates
    val error: String? = null,
    val totalUsers: Int = 0,
    val refreshTrigger: Int = 0, // Used to force refresh when needed
    val lastUpdatedUser: Pair<Int, Int>? = null, // userId to month that was updated
    val userPayments: Map<Int, List<AcademicYearPayment>> = emptyMap() // userId to payments mapping
)

class GroupDetailsViewModel (
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailsUiState())
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()

    // Expose search query for UI with debounce
    @OptIn(FlowPreview::class)
    val searchQuery: StateFlow<String> = _searchQuery
        .debounce(300)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // Paging configuration
    private val pagingConfig = PagingConfig(
        pageSize = 1000, // Large page size to allow unlimited students
        prefetchDistance = 50,
        initialLoadSize = 1000,
        enablePlaceholders = false
    )

    // Users flow with search and month filter support
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val users: StateFlow<PagingData<User>> = combine(
        _uiState.map { it.groupId to it.selectedMonth }.distinctUntilChanged(),
        searchQuery
    ) { pair, query ->
        pair to query
    }
        .flatMapLatest { (pair, query) ->
            val (groupId, selectedMonth) = pair
            
            // Always get all users regardless of month selection
            // The grouping by payment status will be handled in the UI layer
            Pager(
                config = pagingConfig,
                pagingSourceFactory = {
                    repository.getUsersPagingSource(
                        groupId = groupId,
                        searchQuery = query,
                        month = null  // Always get all users
                    )
                }
            ).flow
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )

    
    fun setGroupId(groupId: Int) {
        _uiState.value = _uiState.value.copy(groupId = groupId)
        loadUsersForGroup(groupId)
    }

    private fun loadUsersForGroup(groupId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val users = repository.getUsersByGroupId(groupId).first()
                
                // Fetch payments for all users in the group
                val currentAcademicYear = com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
                val userPaymentsMap = mutableMapOf<Int, List<AcademicYearPayment>>()
                
                users.forEach { user ->
                    try {
                        val payments = repository.getUserPayments(user.uid, currentAcademicYear)
                        userPaymentsMap[user.uid] = payments
                    } catch (e: Exception) {
                        Log.e("GroupDetailsViewModel", "Error fetching payments for user ${user.uid}", e)
                        userPaymentsMap[user.uid] = emptyList()
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalUsers = users.size,
                    userPayments = userPaymentsMap
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load users: ${e.localizedMessage}"
                )
            }
        }
    }
    
    // Refresh user payments data
    private fun refreshUserPayments() {
        viewModelScope.launch {
            try {
                val currentGroupId = _uiState.value.groupId
                if (currentGroupId > 0) {
                    val users = repository.getUsersByGroupId(currentGroupId).first()
                    val currentAcademicYear = com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
                    val userPaymentsMap = mutableMapOf<Int, List<AcademicYearPayment>>()
                    
                    users.forEach { user ->
                        try {
                            val payments = repository.getUserPayments(user.uid, currentAcademicYear)
                            userPaymentsMap[user.uid] = payments
                        } catch (e: Exception) {
                            Log.e("GroupDetailsViewModel", "Error refreshing payments for user ${user.uid}", e)
                            userPaymentsMap[user.uid] = emptyList()
                        }
                    }
                    
                    _uiState.update { it.copy(userPayments = userPaymentsMap) }
                }
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error refreshing user payments", e)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                error = null
            )
        }
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update user: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Function to trigger a refresh of the user list
    fun refreshUserList() {
        _uiState.update { currentState ->
            currentState.copy(
                refreshTrigger = currentState.refreshTrigger + 1
            )
        }
    }

    fun updateSelectedMonth(month: Int?) {
        viewModelScope.launch {
            // First update the state
            _uiState.update { currentState ->
                currentState.copy(
                    selectedMonth = month,
                    error = null,
                    isRefreshing = true,  // Show loading immediately
                    refreshTrigger = currentState.refreshTrigger + 1
                )
            }

            // Force refresh the paging data with a small delay to ensure UI updates
            _searchQuery.update { it }

            // Reset loading state after a short delay
            delay(300) // Small delay to ensure smooth UI update
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true) }
                // Force refresh by updating the search query
                val currentQuery = _searchQuery.value
                _searchQuery.value = "$currentQuery "
                delay(50)
                _searchQuery.value = currentQuery
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun forceRefreshUsers() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true) }
                val currentGroupId = _uiState.value.groupId
                if (currentGroupId > 0) {
                    loadUsersForGroup(currentGroupId)
                    // Force refresh by updating the search query
                    val currentQuery = _searchQuery.value
                    _searchQuery.value = "$currentQuery "
                    delay(50)
                    _searchQuery.value = currentQuery
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Refreshes the user list while preserving the current search and month filter state
     */
    fun refreshWithCurrentState() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    refreshTrigger = currentState.refreshTrigger + 1,
                    isRefreshing = false
                )
            }
            // Force refresh the paging data
            _searchQuery.update { it }
        }
    }

    fun toggleUserFlag(userId: Int, month: Int, isPaid: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            // Save current state
            val currentQuery = _searchQuery.value
            val updateKey = userId to month
            val currentAcademicYear = com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
            
            // Start loading for this specific update
            _uiState.update {
                it.copy(
                    isUpdatingPayment = true,
                    lastUpdatedUser = updateKey,
                    error = null
                )
            }

            // Optimistic UI update - update local payments immediately
            val currentUserPayments = _uiState.value.userPayments
            val userExistingPayments = currentUserPayments[userId] ?: emptyList()
            
            val updatedPayments = if (isPaid) {
                // Add or update payment
                val existingPayment = userExistingPayments.find { it.month == month && it.academicYear == currentAcademicYear }
                if (existingPayment != null) {
                    userExistingPayments.map { payment ->
                        if (payment.month == month && payment.academicYear == currentAcademicYear) {
                            payment.copy(isPaid = true, paymentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()))
                        } else {
                            payment
                        }
                    }
                } else {
                    // Create new payment entry
                    try {
                        val academicYearMonths = com.lee.timely.util.AcademicYearUtils.getAcademicYearMonths(currentAcademicYear)
                        val monthYearPair = academicYearMonths.find { it.first == month }
                        if (monthYearPair != null) {
                            val year = monthYearPair.second
                            val newPayment = AcademicYearPayment(
                                userId = userId,
                                academicYear = currentAcademicYear,
                                month = month,
                                year = year,
                                isPaid = true,
                                paymentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            )
                            userExistingPayments + newPayment
                        } else {
                            userExistingPayments
                        }
                    } catch (e: Exception) {
                        userExistingPayments
                    }
                }
            } else {
                // Remove payment (set as unpaid)
                userExistingPayments.map { payment ->
                    if (payment.month == month && payment.academicYear == currentAcademicYear) {
                        payment.copy(isPaid = false, paymentDate = null)
                    } else {
                        payment
                    }
                }
            }
            
            // Update UI immediately with optimistic changes
            val updatedPaymentsMap = currentUserPayments.toMutableMap()
            updatedPaymentsMap[userId] = updatedPayments
            _uiState.update { it.copy(userPayments = updatedPaymentsMap) }

            try {
                // Perform the update with a timeout to prevent hanging
                withContext(Dispatchers.IO) {
                    withTimeoutOrNull(3000) {  // Reduced timeout to 3 seconds
                        repository.updatePaymentStatus(userId, currentAcademicYear, month, isPaid, if (isPaid) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) else null)
                    } ?: throw Exception("Update timed out")
                }

                // Update the UI state to clear loading
                _uiState.update { currentState ->
                    currentState.copy(
                        isUpdatingPayment = false,
                        lastUpdatedUser = null,
                        refreshTrigger = currentState.refreshTrigger + 1
                    )
                }

                // Refresh the data in the background to ensure consistency
                launch(Dispatchers.IO) {
                    delay(500) // Small delay to ensure database update is complete
                    refreshUserPayments()
                    
                    // Also refresh paging data
                    val tempQuery = currentQuery.ifEmpty { " " }
                    _searchQuery.value = tempQuery
                    delay(50)
                    _searchQuery.value = currentQuery
                }

            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error updating payment status", e)
                
                // Revert optimistic update on error
                val revertedPaymentsMap = currentUserPayments.toMutableMap()
                revertedPaymentsMap[userId] = userExistingPayments
                _uiState.update { currentState ->
                    if (currentState.lastUpdatedUser == updateKey) {
                        currentState.copy(
                            error = "Failed to update: ${e.localizedMessage}",
                            isUpdatingPayment = false,
                            lastUpdatedUser = null,
                            userPayments = revertedPaymentsMap
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }
    
    // Get payments for a specific user
    fun getUserPayments(userId: Int): List<AcademicYearPayment> {
        return _uiState.value.userPayments[userId] ?: emptyList()
    }
}

// ViewModel Factory
class GroupDetailsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 