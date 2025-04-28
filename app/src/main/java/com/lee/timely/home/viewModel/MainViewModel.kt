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

    fun loadUsers() {
        viewModelScope.launch {
            repository.getAll().collect {
                _users.value = it
            }
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
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

class MainViewModelFactory(private val repository: Repository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
