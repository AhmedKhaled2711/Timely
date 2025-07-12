package com.lee.timely.util

sealed class ActivationResult {
    data class Success(val message: String) : ActivationResult()
    data class Error(val message: String) : ActivationResult()
}

// Add LicenseValidationResult sealed class
sealed class LicenseValidationResult {
    object Valid : LicenseValidationResult()
    data class Invalid(val reason: String) : LicenseValidationResult()
}
