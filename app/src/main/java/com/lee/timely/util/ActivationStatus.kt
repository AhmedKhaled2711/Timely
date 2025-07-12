package com.lee.timely.util

sealed class ActivationStatus {
    object Activated : ActivationStatus()
    object NotActivated : ActivationStatus()
} 