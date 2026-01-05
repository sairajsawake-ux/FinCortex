package com.example.fincortex.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val monthlyBudget: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
