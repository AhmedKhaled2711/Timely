package com.lee.timely.features.group.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lee.timely.model.Repository
import com.lee.timely.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class GroupDetailsUiState(
    val groupId: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalUsers: Int = 0
)

class GroupDetailsViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailsUiState())
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Fallback: Direct users list for debugging
    private val _directUsers = MutableStateFlow<List<User>>(emptyList())
    val directUsers: StateFlow<List<User>> = _directUsers.asStateFlow()

    // Paging configuration
    private val pagingConfig = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false,
        prefetchDistance = 5
    )

    // Users flow with search support
    val users: StateFlow<PagingData<User>> = _uiState
        .map { it.groupId }
        .distinctUntilChanged()
        .flatMapLatest { groupId ->
            searchQuery
                .debounce(300) // Debounce search input
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    Pager(
                        config = pagingConfig,
                        pagingSourceFactory = {
                            repository.getUsersPagingSource(
                                groupId = groupId,
                                searchQuery = query
                            )
                        }
                    ).flow
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
                _directUsers.value = users
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
        _uiState.value = _uiState.value.copy(searchQuery = query)
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
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshUsers() {
        // Trigger a refresh by updating the search query
        // This will cause the Pager to reload with the current search query
        val currentQuery = _searchQuery.value
        _searchQuery.value = ""
        // Use a small delay to ensure the empty query is processed
        viewModelScope.launch {
            kotlinx.coroutines.delay(50)
            _searchQuery.value = currentQuery
        }
    }

    fun forceRefreshUsers() {
        // Force a complete refresh by reloading users for the current group
        val currentGroupId = _uiState.value.groupId
        if (currentGroupId > 0) {
            loadUsersForGroup(currentGroupId)
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