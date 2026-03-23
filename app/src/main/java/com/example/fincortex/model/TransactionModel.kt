package com.example.fincortex.model

/**
 * Represents a financial transaction parsed from SMS or entered manually.
 * Stored in Firestore collection: "transactions"
 */
data class TransactionModel(
    val transactionId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val merchant: String = "",
    val category: String = "Others",
    val date: String = "",
    val type: String = "debit",        // "debit" or "credit"
    val createdAt: Long = System.currentTimeMillis(),
    val isFromSMS: Boolean = false,
    val description: String = ""
)
