package com.example.fincortex.model

data class Expense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Long = System.currentTimeMillis(),
    val paymentMode: String = "", // Cash / UPI / Card
    val note: String = ""
)
