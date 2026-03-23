package com.example.fincortex.model

data class InsightResponse(
    val total_spent: Double,
    val highest_category: String,
    val message: String
)