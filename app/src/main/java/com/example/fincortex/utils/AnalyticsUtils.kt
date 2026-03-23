package com.example.fincortex.utils

import com.example.fincortex.model.TransactionModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Prepares chart-ready data structures from raw [TransactionModel] lists.
 * All functions are pure (no side effects) and safe to call from Compose.
 */
object AnalyticsUtils {

    data class CategoryData(
        val category: String,
        val amount: Float,
        val percentage: Float
    )

    data class MonthlyData(
        val label: String,   // short month name e.g. "Jan"
        val amount: Float    // total debit amount for that month
    )

    /**
     * Returns debit spending grouped by category, sorted highest-first.
     * [percentage] is the share of total debit spend (0–100).
     */
    fun categoryChartData(transactions: List<TransactionModel>): List<CategoryData> {
        val debits = transactions.filter { it.type == "debit" }
        val total = debits.sumOf { it.amount }.toFloat().coerceAtLeast(1f)
        return debits
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }
            .entries
            .sortedByDescending { it.value }
            .map { (cat, amt) -> CategoryData(cat, amt, amt / total * 100f) }
    }

    /**
     * Returns total debit spending for each of the last [monthCount] calendar
     * months, oldest first — suitable for a bar chart X-axis.
     */
    fun monthlyChartData(
        transactions: List<TransactionModel>,
        monthCount: Int = 6
    ): List<MonthlyData> {
        val fmt = SimpleDateFormat("MMM", Locale.getDefault())

        // Build year-month key → total debit map
        val monthMap = transactions
            .filter { it.type == "debit" }
            .groupBy {
                val c = Calendar.getInstance().apply { timeInMillis = it.createdAt }
                "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}"
            }
            .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }

        // Walk backwards monthCount months, oldest first
        return (monthCount - 1 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            val key = "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}"
            MonthlyData(label = fmt.format(c.time), amount = monthMap[key] ?: 0f)
        }
    }
}
