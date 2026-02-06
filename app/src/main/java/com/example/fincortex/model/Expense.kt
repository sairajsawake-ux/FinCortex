package com.example.fincortex.model

data class Expense(
    val title: String,
    val amount: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
