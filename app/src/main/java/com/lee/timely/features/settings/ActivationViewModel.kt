package com.lee.timely.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivationUiState(
    val licenseKey: String = "",
    val isLoading: Boolean = false,
    val message: String = "",
    val messageColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Green,
    val isActivated: Boolean = false,
    val activationInfo: String = "",
    val deviceInfo: String = ""
)

class ActivationViewModel : ViewModel() {
    lateinit var context: Context

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState())
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState.asStateFlow()

    fun updateLicenseKey(key: String) {
        _uiState.value = _uiState.value.copy(licenseKey = key)
    }

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }

    fun setMessage(message: String, color: androidx.compose.ui.graphics.Color) {
        _uiState.value = _uiState.value.copy(message = message, messageColor = color)
    }

    fun setActivated(activated: Boolean, info: String = "") {
        _uiState.value = _uiState.value.copy(
            isActivated = activated,
            activationInfo = info
        )
    }

    fun setDeviceInfo(info: String) {
        _uiState.value = _uiState.value.copy(deviceInfo = info)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = "")
    }

    fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                setMessage("Error: ${e.localizedMessage}", androidx.compose.ui.graphics.Color.Red)
            }
        }
    }


    fun onGoogleSignInResult(idToken: String?) {
        if (idToken == null) {
            _googleSignInState.value = GoogleSignInState(
                error = "Google Sign-In failed: No ID token received. Please try again."
            )
            return
        }
        
        if (idToken.isBlank()) {
            _googleSignInState.value = GoogleSignInState(
                error = "Google Sign-In failed: Invalid ID token. Please try again."
            )
            return
        }
        
        setLoading(true)
        val auth = FirebaseAuth.getInstance()
        
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            _googleSignInState.value = GoogleSignInState(success = true)
                        } else {
                            _googleSignInState.value = GoogleSignInState(
                                error = "Authentication succeeded but user data is unavailable."
                            )
                        }
                    } else {
                        val exception = task.exception
                        val errorMessage = when {
                            exception == null -> "Authentication failed. Please try again."
                            exception.message?.contains("network", ignoreCase = true) == true -> 
                                "Network error. Please check your internet connection and try again."
                            exception.message?.contains("invalid", ignoreCase = true) == true -> 
                                "Invalid credentials. Please sign in again."
                            exception.message?.contains("quota", ignoreCase = true) == true -> 
                                "Service temporarily unavailable. Please try again later."
                            else -> exception.localizedMessage ?: "Authentication failed. Please try again."
                        }
                        _googleSignInState.value = GoogleSignInState(error = errorMessage)
                    }
                }
                .addOnFailureListener { exception ->
                    setLoading(false)
                    val errorMessage = when {
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your internet connection and try again."
                        exception.message?.contains("invalid", ignoreCase = true) == true -> 
                            "Invalid credentials. Please sign in again."
                        else -> exception.localizedMessage ?: "Authentication failed. Please try again."
                    }
                    _googleSignInState.value = GoogleSignInState(error = errorMessage)
                }
        } catch (e: Exception) {
            setLoading(false)
            _googleSignInState.value = GoogleSignInState(
                error = "Unexpected error: ${e.localizedMessage ?: "Please try again."}"
            )
        }
    }

    data class GoogleSignInState(val success: Boolean = false, val error: String? = null)
} 