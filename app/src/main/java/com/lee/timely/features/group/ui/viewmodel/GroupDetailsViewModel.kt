package com.lee.timely.features.group.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lee.timely.domain.Repository
import com.lee.timely.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

data class GroupDetailsUiState(
    val groupId: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedMonth: Int? = null,
    val totalUsers: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val shouldShowError: Boolean get() = error != null
}

class GroupDetailsViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailsUiState())
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _refreshTrigger = MutableStateFlow(0)
    
    // Clear error after 5 seconds
    private fun clearErrorAfterDelay() {
        viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(error = null) }
        }
    }

    // Paging configuration
    private val pagingConfig = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false,
        prefetchDistance = 5
    )

    // Users flow with search support
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val users: StateFlow<PagingData<User>> = _refreshTrigger
        .flatMapLatest { _ ->
            combine(
                _uiState.map { it.groupId }.distinctUntilChanged(),
                _searchQuery
                    .debounce(300)
                    .distinctUntilChanged()
            ) { groupId, query ->
                groupId to query
            }
        }
        .flatMapLatest { (groupId, query) ->
            Pager(
                config = pagingConfig,
                pagingSourceFactory = {
                    repository.getUsersPagingSource(
                        groupId = groupId,
                        searchQuery = query.ifEmpty { "" }
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
        _uiState.update { it.copy(groupId = groupId) }
        refresh()
    }
    
    fun updateSelectedMonth(month: Int?) {
        _uiState.update { it.copy(selectedMonth = month) }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        updateSearchQuery("")
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.updateUser(user)
                // Refresh the list after update
                refresh()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update user: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
                clearErrorAfterDelay()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true) }
                // Increment refresh trigger to force paging source recreation
                _refreshTrigger.value++
                _uiState.update { it.copy(
                    isRefreshing = false,
                    lastUpdated = System.currentTimeMillis()
                )}
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to refresh: ${e.localizedMessage}",
                        isRefreshing = false
                    )
                }
                clearErrorAfterDelay()
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