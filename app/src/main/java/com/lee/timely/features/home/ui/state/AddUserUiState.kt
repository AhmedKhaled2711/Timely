package com.lee.timely.features.home.ui.state

import com.lee.timely.domain.User

/**
 * Sealed class representing the UI state for add user operation
 */
sealed class AddUserUiState {
    object Idle : AddUserUiState()
    object Loading : AddUserUiState()
    data class Success(val user: User) : AddUserUiState()
    data class Error(val message: String) : AddUserUiState()
}

/**
 * One-time UI events for navigation and snackbar
 */
sealed class AddUserUiEvent {
    data class NavigateBack(val user: User) : AddUserUiEvent()
    data class ShowSnackbar(val message: String) : AddUserUiEvent()
    object None : AddUserUiEvent()
}
