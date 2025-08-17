package com.lee.timely.util

/**
 * Represents the state of a payment update operation.
 */
sealed class PaymentUpdateState {
    /** Initial state, no operation in progress */
    object Idle : PaymentUpdateState()

    /** Payment update is in progress for a specific user and month */
    data class Loading(
        val userId: Int,
        val month: Int
    ) : PaymentUpdateState()

    /** Payment update completed successfully */
    data class Success(
        val userId: Int,
        val month: Int,
        val isPaid: Boolean
    ) : PaymentUpdateState()

    /** Payment update failed */
    data class Error(
        val userId: Int,
        val month: Int,
        val message: String
    ) : PaymentUpdateState()

    /**
     * Check if an update is in progress for a specific user and month
     * @param userId The user ID to check
     * @param month The month to check (can be null to check for any month for this user)
     * @return true if there's an update in progress for this user (and month if specified)
     */
    fun isUpdating(userId: Int, month: Int?): Boolean {
        return when (this) {
            is Loading -> this.userId == userId && (month == null || this.month == month)
            else -> false
        }
    }

    /** Get the current updating user and month if any */
    val currentUpdate: Pair<Int, Int>?
        get() = when (this) {
            is Loading -> Pair(userId, month)
            is Success -> Pair(userId, month)
            is Error -> Pair(userId, month)
            else -> null
        }
}