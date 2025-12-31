package com.lee.timely.features.home.ui.state

import com.lee.timely.domain.User

sealed class TransferUserUiState {
    object Idle : TransferUserUiState()
    object Loading : TransferUserUiState()
    object Success : TransferUserUiState()
    data class Error(val message: String) : TransferUserUiState()
}

sealed class TransferUserUiEvent {
    data class ShowSnackbar(val message: String) : TransferUserUiEvent()
    data class NavigateBack(val user: User) : TransferUserUiEvent()
    object None : TransferUserUiEvent()
}
