package com.lee.timely.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lee.timely.model.GroupName
import com.lee.timely.model.Repository
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage

    private var currentPage = 0
    private val pageSize = 20

    // Add this function to reset and load initial users
    fun loadInitialUsers(groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Reset pagination state
                currentPage = 0
                _isLastPage.value = false
                _users.value = emptyList()

                // Load first page
                val initialUsers = repository.getUsersByGroupIdPaginated(
                    groupId = groupId,
                    page = currentPage,
                    pageSize = pageSize
                )

                _users.value = initialUsers
                currentPage++

                // Check if this is all the data we have
                if (initialUsers.size < pageSize) {
                    _isLastPage.value = true
                }
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
                val newUsers = repository.getUsersByGroupIdPaginated(
                    groupId = groupId,
                    page = currentPage,
                    pageSize = pageSize
                )

                if (newUsers.isEmpty()) {
                    _isLastPage.value = true
                } else {
                    _users.value = _users.value + newUsers
                    currentPage++

                    // Early detection of last page
                    if (newUsers.size < pageSize) {
                        _isLastPage.value = true
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add this function to refresh users while maintaining pagination state
    fun refreshUsers(groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val refreshedUsers = repository.getUsersByGroupIdPaginated(
                    groupId = groupId,
                    page = 0, // Always load first page for refresh
                    pageSize = (currentPage + 1) * pageSize // Load all pages we've loaded so far
                )

                _users.value = refreshedUsers

                // Check if we have more data to load
                if (refreshedUsers.size < (currentPage + 1) * pageSize) {
                    _isLastPage.value = true
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
            // Refresh the list after adding a new user
            user.groupId?.let { refreshUsers(it) }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            val groupId = user.groupId
            repository.deleteUser(user)
            // Refresh the list after deletion
            groupId?.let { refreshUsers(it) }
        }
    }

    private val _schoolYears = MutableStateFlow<List<GradeYear>>(emptyList())
    val schoolYears: StateFlow<List<GradeYear>> = _schoolYears

    init {
        loadSchoolYears()
    }

    private fun loadSchoolYears() {
        viewModelScope.launch {
            repository.getAllSchoolYears().collect {
                _schoolYears.value = it
            }
        }
    }

    fun insertSchoolYear(schoolYear: GradeYear) {
        viewModelScope.launch {
            repository.insertSchoolYear(schoolYear)
            loadSchoolYears()
        }
    }

    fun deleteSchoolYear(schoolYear: GradeYear) {
        viewModelScope.launch {
            repository.deleteSchoolYear(schoolYear)
            loadSchoolYears()
        }
    }

    fun getGroupsForYear(schoolYearId: Int): Flow<List<GroupName>> {
        return repository.getGroupsForSchoolYearId(schoolYearId)
    }

    fun getGroupById(groupId: Int): Flow<GroupName?> {
        return repository.getGroupById(groupId)
    }

    fun addGroupToYear(schoolYearId: Int, groupName: String) {
        viewModelScope.launch {
            repository.insertGroup(GroupName(groupName = groupName, schoolYearId = schoolYearId))
        }
    }

    fun deleteGroup(group: GroupName) {
        viewModelScope.launch {
            repository.deleteGroup(group)
        }
    }

    fun getUsersByGroup(groupId: Int): Flow<List<User>> {
        return repository.getUsersByGroupId(groupId)
    }

    fun toggleUserFlag(userId: Int, flagNumber: Int, newValue: Boolean) {
        viewModelScope.launch {
            when (flagNumber) {
                1 -> repository.updateFlag1(userId, newValue)
                2 -> repository.updateFlag2(userId, newValue)
                3 -> repository.updateFlag3(userId, newValue)
                4 -> repository.updateFlag4(userId, newValue)
                5 -> repository.updateFlag5(userId, newValue)
                6 -> repository.updateFlag6(userId, newValue)
            }
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
