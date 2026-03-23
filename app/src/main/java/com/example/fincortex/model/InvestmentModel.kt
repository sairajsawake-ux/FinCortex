package com.example.fincortex.model

/**
 * Represents a user's investment entry.
 * Stored in Firestore collection: "investments"
 */
data class InvestmentModel(
    val investmentId: String = "",
    val userId: String = "",
    val name: String = "",           // e.g. "Nifty 50 Index Fund"
    val amount: Double = 0.0,        // principal invested
    val type: String = "Stocks",     // Stocks | Mutual Fund | SIP | FD | Crypto | Gold | Others
    val returns: Double = 0.0,       // current value / returns (can be negative)
    val date: String = "",           // ISO date string of investment date
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
