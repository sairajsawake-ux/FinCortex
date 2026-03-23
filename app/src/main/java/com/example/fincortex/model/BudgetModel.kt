package com.example.fincortex.model

/**
 * Represents the monthly budget for a user.
 * Stored in Firestore collection: "budgets"
 */
data class BudgetModel(
    val userId: String = "",
    val monthlyLimit: Double = 0.0,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val limitExceeded: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
