package com.lee.timely.features.home.viewmodel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lee.timely.model.GroupName
import com.lee.timely.model.Repository
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val repository: Repository) : ViewModel() {
    
    // Public getter for repository to be used in activation
    val repositoryInstance: Repository get() = repository

    // User list state with pagination
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20

    // School years state
    private val _schoolYears = MutableStateFlow<List<GradeYear>>(emptyList())
    val schoolYears: StateFlow<List<GradeYear>> = _schoolYears.asStateFlow()

    private val _isSchoolYearsLoading = MutableStateFlow(true)
    val isSchoolYearsLoading: StateFlow<Boolean> = _isSchoolYearsLoading.asStateFlow()

    private val _isGroupsLoading = MutableStateFlow(true)
    val isGroupsLoading: StateFlow<Boolean> = _isGroupsLoading

    init {
        loadSchoolYears()
    }

    // Reset error state
    fun resetError() {
        _error.value = null
    }

    // User operations with optimized coroutines
    fun loadInitialUsers(groupId: Int) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    // Reset pagination state
                    currentPage = 0
                    _isLastPage.value = false

                    // Load first page
                    val initialUsers = repository.getUsersByGroupIdPaginated(
                        groupId = groupId,
                        page = currentPage,
                        pageSize = pageSize
                    )

                    _users.value = initialUsers
                    currentPage++

                    // Check if this is all the data
                    if (initialUsers.size < pageSize) {
                        _isLastPage.value = true
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load users: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreUsers(groupId: Int) {
        if (_isLoading.value || _isLastPage.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    val newUsers = repository.getUsersByGroupIdPaginated(
                        groupId = groupId,
                        page = currentPage,
                        pageSize = pageSize
                    )

                    if (newUsers.isEmpty()) {
                        _isLastPage.value = true
                    } else {
                        _users.update { current -> current + newUsers }
                        currentPage++

                        if (newUsers.size < pageSize) {
                            _isLastPage.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load more users: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun refreshUsers(groupId: Int) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    val refreshedUsers = repository.getUsersByGroupIdPaginated(
                        groupId = groupId,
                        page = 0,
                        pageSize = (currentPage + 1) * pageSize
                    )

                    _users.value = refreshedUsers
                    _isLastPage.value = refreshedUsers.size < (currentPage + 1) * pageSize
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh users: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.insertUser(user)
                }
                user.groupId?.let { refreshUsers(it) }
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
                user.groupId?.let { refreshUsers(it) }
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
                // Update local state immediately
                _users.update { currentUsers ->
                    currentUsers.map { user ->
                        if (user.uid == userId) {
                            when (flagNumber) {
                                1 -> user.copy(flag1 = newValue)
                                2 -> user.copy(flag2 = newValue)
                                3 -> user.copy(flag3 = newValue)
                                4 -> user.copy(flag4 = newValue)
                                5 -> user.copy(flag5 = newValue)
                                6 -> user.copy(flag6 = newValue)
                                7 -> user.copy(flag7 = newValue)
                                8 -> user.copy(flag8 = newValue)
                                9 -> user.copy(flag9 = newValue)
                                10 -> user.copy(flag10 = newValue)
                                11 -> user.copy(flag11 = newValue)
                                12 -> user.copy(flag12 = newValue)
                                else -> user
                            }
                        } else {
                            user
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update flag: ${e.localizedMessage}"
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            user.groupId?.let { refreshUsers(it) }
        }
    }
}

class MainViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
