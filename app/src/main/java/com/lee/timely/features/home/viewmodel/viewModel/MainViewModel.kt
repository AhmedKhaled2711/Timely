package com.lee.timely.features.home.viewmodel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.Repository
import com.lee.timely.domain.GradeYear
import com.lee.timely.domain.User
import com.lee.timely.domain.AcademicYearPayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.app.Application
import android.content.Context
import com.lee.timely.R
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.lee.timely.features.home.ui.state.AddUserUiState
import com.lee.timely.features.home.ui.state.AddUserUiEvent
import com.lee.timely.features.home.ui.state.TransferUserUiState
import com.lee.timely.features.home.ui.state.TransferUserUiEvent

class MainViewModel(private val repository: Repository, private val application: Application) : ViewModel() {
    
    // Public getter for repository to be used in activation
    val repositoryInstance: Repository get() = repository

    // Add User UI State and Events
    private val _addUserUiState = MutableStateFlow<AddUserUiState>(AddUserUiState.Idle)
    val addUserUiState: StateFlow<AddUserUiState> = _addUserUiState.asStateFlow()
    
    private val _addUserEvent = Channel<AddUserUiEvent>(Channel.BUFFERED)
    val addUserEvent = _addUserEvent.receiveAsFlow()

    // Transfer User UI State and Events
    private val _transferUserUiState = MutableStateFlow<TransferUserUiState>(TransferUserUiState.Idle)
    val transferUserUiState: StateFlow<TransferUserUiState> = _transferUserUiState.asStateFlow()
    
    private val _transferUserEvent = Channel<TransferUserUiEvent>(Channel.BUFFERED)
    val transferUserEvent = _transferUserEvent.receiveAsFlow()

    // User list state with pagination
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // --- Duplicate Validation State ---
    private val _duplicateNameError = MutableStateFlow<String?>(null)
    val duplicateNameError: StateFlow<String?> = _duplicateNameError.asStateFlow()
    
    private var currentPage = 0
    private val pageSize = 1000 // Large page size to allow unlimited students

    // School years state
    private val _schoolYears = MutableStateFlow<List<GradeYear>>(emptyList())
    val schoolYears: StateFlow<List<GradeYear>> = _schoolYears.asStateFlow()
    
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()
    
    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

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
    
    // Reset duplicate name error state
    fun resetDuplicateError() {
        _duplicateNameError.value = null
    }
    
    // Reset add user UI state
    fun resetAddUserUiState() {
        _addUserUiState.value = AddUserUiState.Idle
    }
    
    // Reset transfer user UI state
    fun resetTransferUserUiState() {
        _transferUserUiState.value = TransferUserUiState.Idle
    }

    // User operations with optimized coroutines
    fun loadInitialUsers(groupId: Int) {
        viewModelScope.launch {
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
            }
        }
    }

    fun loadMoreUsers(groupId: Int) {
        if (_isLastPage.value) return

        viewModelScope.launch {
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
            }
        }
    }

    fun refreshUsers(groupId: Int) {
        viewModelScope.launch {
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
            }
        }
    }

    fun addUser(user: User, context: Context) {
        viewModelScope.launch {
            try {
                _addUserUiState.value = AddUserUiState.Loading
                
                withContext(Dispatchers.IO) {
                    // Check for duplicate student name
                    val isDuplicate = repository.isDuplicateStudentName(user.groupId ?: 0, user.allName)
                    if (isDuplicate) {
                        _addUserUiState.value = AddUserUiState.Error(
                            context.getString(R.string.error_duplicate_student_name)
                        )
                        _addUserEvent.trySend(AddUserUiEvent.ShowSnackbar(
                            context.getString(R.string.error_duplicate_student_name)
                        ))
                        return@withContext
                    }
                    
                    // Insert user if no duplicate found
                    repository.insertUser(user)
                }
                
                // Success - send navigation event only, UI will handle success message
                _addUserEvent.trySend(AddUserUiEvent.NavigateBack(user))
                
                // Refresh users list in background
                user.groupId?.let { 
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            refreshUsers(it)
                        }
                    }
                }
                
            } catch (e: Exception) {
                _addUserUiState.value = AddUserUiState.Error("Failed to add user: ${e.localizedMessage}")
                _addUserEvent.trySend(AddUserUiEvent.ShowSnackbar("Failed to add user: ${e.localizedMessage}"))
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
    
    /**
     * Updates an existing school year in the database
     * @param updatedYear The updated GradeYear object
     */
    fun updateSchoolYear(updatedYear: GradeYear) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateError.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.updateSchoolYear(updatedYear)
                }
                // Refresh the school years list to reflect changes
                loadSchoolYears()
            } catch (e: Exception) {
                _updateError.value = "Failed to update school year: ${e.localizedMessage}"
            } finally {
                _isUpdating.value = false
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

    /**
     * Updates an existing group in the database
     * @param updatedGroup The updated GroupName object
     */
    fun updateGroup(updatedGroup: GroupName) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateError.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.updateGroup(updatedGroup)
                }
            } catch (e: Exception) {
                _updateError.value = "Failed to update group: ${e.localizedMessage}"
            } finally {
                _isUpdating.value = false
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
                    // Use the new academic year payment system
                    repository.updateUserPaymentStatus(userId, flagNumber, newValue)
                }
                // Note: Local state update is no longer needed since the User entity
                // no longer contains flag fields. The UI will get updated payment
                // status from the academic year payments when it recomposes.
            } catch (e: Exception) {
                _error.value = "Failed to update payment status: ${e.localizedMessage}"
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                _addUserUiState.value = AddUserUiState.Loading
                
                withContext(Dispatchers.IO) {
                    repository.updateUser(user)
                }
                
                // Success - send navigation event only, UI will handle success message
                _addUserEvent.trySend(AddUserUiEvent.NavigateBack(user))
                
                // Refresh users list in background
                user.groupId?.let { 
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            refreshUsers(it)
                        }
                    }
                }
                
            } catch (e: Exception) {
                _addUserUiState.value = AddUserUiState.Error("Failed to update user: ${e.localizedMessage}")
                _addUserEvent.trySend(AddUserUiEvent.ShowSnackbar("Failed to update user: ${e.localizedMessage}"))
            }
        }
    }

    // Get user by ID for navigation purposes
    suspend fun getUserById(userId: Int): User? {
        return try {
            withContext(Dispatchers.IO) {
                repository.getUserById(userId)
            }
        } catch (e: Exception) {
            _error.value = "Failed to get user: ${e.localizedMessage}"
            null
        }
    }

    // Get user payments for a specific academic year
    suspend fun getUserPayments(userId: Int, academicYear: String): List<AcademicYearPayment> {
        return try {
            withContext(Dispatchers.IO) {
                repository.getUserPayments(userId, academicYear)
            }
        } catch (e: Exception) {
            _error.value = "Failed to get user payments: ${e.localizedMessage}"
            emptyList()
        }
    }

    // Get school year ID for a specific group
    fun getSchoolYearIdForGroup(groupId: Int): Flow<Int?> {
        return repository.getGroupById(groupId)
            .map { group -> group?.schoolYearId }
            .flowOn(Dispatchers.IO)
            .catch { e -> _error.value = "Failed to get school year for group: ${e.localizedMessage}" }
    }

    // Transfer user to different group
    fun transferUser(user: User, newGroupId: Int, context: Context) {
        viewModelScope.launch {
            try {
                _transferUserUiState.value = TransferUserUiState.Loading
                
                withContext(Dispatchers.IO) {
                    // Check if user with same name already exists in target group
                    val isDuplicate = repository.isDuplicateStudentName(newGroupId, user.allName)
                    if (isDuplicate) {
                        _transferUserUiState.value = TransferUserUiState.Error(
                            context.getString(R.string.error_duplicate_student_name_target_group)
                        )
                        _transferUserEvent.trySend(TransferUserUiEvent.ShowSnackbar(
                            context.getString(R.string.error_duplicate_student_name_target_group)
                        ))
                        return@withContext
                    }
                    
                    // Update user's group
                    val updatedUser = user.copy(groupId = newGroupId)
                    repository.updateUser(updatedUser)
                }
                
                // Success
                _transferUserUiState.value = TransferUserUiState.Success
                _transferUserEvent.trySend(TransferUserUiEvent.ShowSnackbar(
                    context.getString(R.string.user_transferred_successfully)
                ))
                _transferUserEvent.trySend(TransferUserUiEvent.NavigateBack(user))
                
                // Refresh users list in background
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        refreshUsers(newGroupId)
                    }
                }
                
            } catch (e: Exception) {
                _transferUserUiState.value = TransferUserUiState.Error("Failed to transfer user: ${e.localizedMessage}")
                _transferUserEvent.trySend(TransferUserUiEvent.ShowSnackbar("Failed to transfer user: ${e.localizedMessage}"))
            }
        }
    }
}

class MainViewModelFactory(private val repository: Repository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
