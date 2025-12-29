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
    val lastUpdatedUser: Pair<Int, Int>? = null // userId to month that was updated
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
            val academicYear = if (selectedMonth != null) {
                // Get academic year for the selected month using current year
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                com.lee.timely.util.AcademicYearUtils.getAcademicYearForMonth(selectedMonth, currentYear)
            } else {
                com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
            }
            
            Pager(
                config = pagingConfig,
                pagingSourceFactory = {
                    if (selectedMonth != null) {
                        // Get only paid users for the selected month and academic year
                        repository.getUsersByPaymentStatusPagingSource(
                            groupId = groupId,
                            searchQuery = query.ifEmpty { null },
                            month = selectedMonth,
                            isPaid = true,
                            academicYear = academicYear
                        )
                    } else {
                        // If no month selected, get all users
                        repository.getUsersPagingSource(
                            groupId = groupId,
                            searchQuery = query,
                            month = null
                        )
                    }
                }
            ).flow
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )

    // Separate flow for unpaid users when a month is selected
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val unpaidUsers: StateFlow<PagingData<User>> = combine(
        _uiState.map { it.groupId to it.selectedMonth }.distinctUntilChanged(),
        searchQuery
    ) { pair, query ->
        pair to query
    }
        .flatMapLatest { (pair, query) ->
            val (groupId, selectedMonth) = pair
            try {
                val searchQuery = query.ifEmpty { null }
                
                if (selectedMonth == null) {
                    return@flatMapLatest flow { emit(PagingData.empty<User>()) }
                }
                
                // Get academic year for the selected month using current year
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val academicYear = com.lee.timely.util.AcademicYearUtils.getAcademicYearForMonth(selectedMonth, currentYear)

                Log.d(
                    "GroupDetailsViewModel",
                    "Checking for paid users in group $groupId, month $selectedMonth, academic year $academicYear"
                )
                val hasPaidUsers = repository.hasPaidUsersForMonth(groupId, selectedMonth, academicYear)
                Log.d("GroupDetailsViewModel", "Has paid users: $hasPaidUsers")

                Pager(pagingConfig) {
                    if (hasPaidUsers) {
                        // If there are paid users, show only unpaid users for the selected month and academic year
                        repository.getUsersByPaymentStatusPagingSource(
                            groupId = groupId,
                            searchQuery = searchQuery,
                            month = selectedMonth,
                            isPaid = false,
                            academicYear = academicYear
                        )
                    } else {
                        // If no paid users, show all users in the group as unpaid
                        repository.getUsersPagingSource(
                            groupId = groupId,
                            searchQuery = searchQuery ?: "",
                            month = null
                        )
                    }
                }.flow
            } catch (e: Exception) {
                Log.e(
                    "GroupDetailsViewModel",
                    "Error in unpaidUsers flow: ${e.message}",
                    e
                )
                flow { emit(PagingData.empty<User>()) }
            }
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalUsers = users.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load users: ${e.localizedMessage}"
                )
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
            
            // Start loading for this specific update
            _uiState.update {
                it.copy(
                    isUpdatingPayment = true,
                    lastUpdatedUser = updateKey,
                    error = null
                )
            }

            try {
                // Perform the update with a timeout to prevent hanging
                withContext(Dispatchers.IO) {
                    withTimeoutOrNull(3000) {  // Reduced timeout to 3 seconds
                        repository.updateUserPaymentStatus(userId, month, isPaid)
                    } ?: throw Exception("Update timed out")
                }

                // Update the UI optimistically without waiting for paging refresh
                _uiState.update { currentState ->
                    currentState.copy(
                        isUpdatingPayment = false,
                        lastUpdatedUser = null,
                        refreshTrigger = currentState.refreshTrigger + 1
                    )
                }

                // Refresh the data in the background
                launch(Dispatchers.IO) {
                    val tempQuery = currentQuery.ifEmpty { " " }
                    _searchQuery.value = tempQuery
                    delay(50)
                    _searchQuery.value = currentQuery
                }

            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error updating payment status", e)
                // Only update error state if this is still the current update
                _uiState.update { currentState ->
                    if (currentState.lastUpdatedUser == updateKey) {
                        currentState.copy(
                            error = "Failed to update: ${e.localizedMessage}",
                            isUpdatingPayment = false,
                            lastUpdatedUser = null
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
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