package com.lee.timely.features.home.viewmodel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lee.timely.model.GroupName
import com.lee.timely.model.Repository
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class EnhancedMainViewModel(private val repository: Repository) : ViewModel() {

    // Paging 3 for users - much better for large datasets
    private var currentGroupId: Int = -1
    
    fun getUsersPagingData(groupId: Int): Flow<PagingData<User>> {
        // Only create new pager if groupId changed
        if (currentGroupId != groupId) {
            currentGroupId = groupId
        }
        
        return Pager(
            config = PagingConfig(
                pageSize = 50, // Load 50 users at a time
                enablePlaceholders = false,
                prefetchDistance = 3 // Prefetch 3 pages ahead
            ),
            pagingSourceFactory = { repository. getUsersPagingSource(groupId) }
        ).flow.cachedIn(viewModelScope)
    }

    // School years state
    private val _schoolYears = MutableStateFlow<List<GradeYear>>(emptyList())
    val schoolYears: StateFlow<List<GradeYear>> = _schoolYears.asStateFlow()

    private val _isSchoolYearsLoading = MutableStateFlow(true)
    val isSchoolYearsLoading: StateFlow<Boolean> = _isSchoolYearsLoading.asStateFlow()

    private val _isGroupsLoading = MutableStateFlow(true)
    val isGroupsLoading: StateFlow<Boolean> = _isGroupsLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSchoolYears()
    }

    // Reset error state
    fun resetError() {
        _error.value = null
    }

    // User operations with optimized coroutines
    fun addUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.insertUser(user)
                }
            } catch (e: Exception) {
                _error.value = "Failed to add user: ${e.localizedMessage}"
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteUser(user)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete user: ${e.localizedMessage}"
            }
        }
    }

    // School year operations
    private fun loadSchoolYears() {
        viewModelScope.launch {
            _isSchoolYearsLoading.value = true
            repository.getAllSchoolYears()
                .flowOn(Dispatchers.IO)
                .catch { e -> _error.value = "Failed to load school years: ${e.localizedMessage}" }
                .collect { years ->
                    _schoolYears.value = years
                    _isSchoolYearsLoading.value = false
                }
        }
    }

    fun insertSchoolYear(schoolYear: GradeYear) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.insertSchoolYear(schoolYear)
                }
            } catch (e: Exception) {
                _error.value = "Failed to add school year: ${e.localizedMessage}"
            }
        }
    }

    fun deleteSchoolYear(schoolYear: GradeYear) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteSchoolYear(schoolYear)
                }
                loadSchoolYears()
            } catch (e: Exception) {
                _error.value = "Failed to delete school year: ${e.localizedMessage}"
            }
        }
    }

    // Group operations
    fun getGroupsForYear(schoolYearId: Int): Flow<List<GroupName>> {
        return repository.getGroupsForSchoolYearId(schoolYearId)
            .onStart { _isGroupsLoading.value = true }
            .onEach { _isGroupsLoading.value = false }
            .flowOn(Dispatchers.IO)
            .catch { e -> _error.value = "Failed to load groups: ${e.localizedMessage}" }
    }

    fun getGroupById(groupId: Int): Flow<GroupName?> {
        return repository.getGroupById(groupId)
            .flowOn(Dispatchers.IO)
            .catch { e -> _error.value = "Failed to load group: ${e.localizedMessage}" }
    }

    fun addGroupToYear(schoolYearId: Int, groupName: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.insertGroup(GroupName(groupName = groupName, schoolYearId = schoolYearId))
                }
            } catch (e: Exception) {
                _error.value = "Failed to add group: ${e.localizedMessage}"
            }
        }
    }

    fun deleteGroup(group: GroupName) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteGroup(group)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete group: ${e.localizedMessage}"
            }
        }
    }

    fun toggleUserFlag(userId: Int, flagNumber: Int, newValue: Boolean) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    when (flagNumber) {
                        1 -> repository.updateFlag1(userId, newValue)
                        2 -> repository.updateFlag2(userId, newValue)
                        3 -> repository.updateFlag3(userId, newValue)
                        4 -> repository.updateFlag4(userId, newValue)
                        5 -> repository.updateFlag5(userId, newValue)
                        6 -> repository.updateFlag6(userId, newValue)
                        7 -> repository.updateFlag7(userId, newValue)
                        8 -> repository.updateFlag8(userId, newValue)
                        9 -> repository.updateFlag9(userId, newValue)
                        10 -> repository.updateFlag10(userId, newValue)
                        11 -> repository.updateFlag11(userId, newValue)
                        12 -> repository.updateFlag12(userId, newValue)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update flag: ${e.localizedMessage}"
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateUser(user)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update user: ${e.localizedMessage}"
            }
        }
    }
}

class EnhancedMainViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnhancedMainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 