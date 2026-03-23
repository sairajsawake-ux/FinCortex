package com.example.fincortex.utils

import com.example.fincortex.model.BudgetModel
import com.example.fincortex.model.TransactionModel
import java.util.Calendar

/**
 * Generates human-readable financial insight messages from transaction history
 * and budget data. Insights are ranked: warnings first, then tips, then info.
 */
object SmartInsights {

    enum class InsightType { WARNING, TIP, INFO }

    data class Insight(val message: String, val type: InsightType)

    /**
     * Produces up to 5 insights ordered by severity (WARNING > TIP > INFO).
     * [totalInvested] is optional; when > 0 an investment insight is included.
     * Existing callers omitting [totalInvested] continue to work unchanged.
     */
    fun generate(
        transactions: List<TransactionModel>,
        budget: BudgetModel?,
        totalInvested: Double = 0.0
    ): List<Insight> {
        val debits = transactions.filter { it.type == "debit" }
        if (debits.isEmpty()) return emptyList()

        val insights = mutableListOf<Insight>()
        val totalSpent = debits.sumOf { it.amount }
        val categoryTotals = debits
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val topCategory = categoryTotals.maxByOrNull { it.value }

        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthlySpent = debits.filter { it.createdAt >= monthStart }.sumOf { it.amount }
        val todayCount = debits.count { it.createdAt >= todayStart }
        val todaySpent = debits.filter { it.createdAt >= todayStart }.sumOf { it.amount }

        // ---- Budget alerts (highest priority) ----
        if (budget != null && budget.monthlyLimit > 0) {
            val usagePct = (budget.spentAmount / budget.monthlyLimit * 100).toInt()
            when {
                budget.limitExceeded ->
                    insights += Insight(
                        "You exceeded your monthly budget of ₹${"%.0f".format(budget.monthlyLimit)}! " +
                                "You spent ₹${"%.0f".format(budget.spentAmount)}.",
                        InsightType.WARNING
                    )
                usagePct >= 90 ->
                    insights += Insight(
                        "Budget alert: ${usagePct}% of your ₹${"%.0f".format(budget.monthlyLimit)} limit used. Spend carefully!",
                        InsightType.WARNING
                    )
                usagePct >= 70 ->
                    insights += Insight(
                        "You're at ${usagePct}% of your monthly budget (₹${"%.0f".format(budget.monthlyLimit)}).",
                        InsightType.INFO
                    )
                else ->
                    insights += Insight(
                        "Great job! You're well within your budget this month.",
                        InsightType.TIP
                    )
            }
        } else {
            insights += Insight(
                "Set a monthly budget to track your savings goals.",
                InsightType.TIP
            )
        }

        // ---- Today's spending ----
        if (todayCount >= 3) {
            insights += Insight(
                "High daily spending: $todayCount transactions today totalling ₹${"%.0f".format(todaySpent)}.",
                InsightType.WARNING
            )
        }

        // ---- Category over-spend checks ----
        val food = categoryTotals["Food"] ?: 0.0
        if (food > 0 && food / totalSpent > 0.35) {
            insights += Insight(
                "You spent too much on food (${(food / totalSpent * 100).toInt()}% of total). " +
                        "Consider home cooking.",
                InsightType.WARNING
            )
        }

        val shopping = categoryTotals["Shopping"] ?: 0.0
        if (shopping > 0 && shopping / totalSpent > 0.30) {
            insights += Insight(
                "Shopping is taking up ${(shopping / totalSpent * 100).toInt()}% of your spending.",
                InsightType.WARNING
            )
        }

        val travel = categoryTotals["Travel"] ?: 0.0
        if (travel > 0 && travel / totalSpent > 0.25) {
            insights += Insight(
                "Travel expenses are high. Try public transport to save.",
                InsightType.TIP
            )
        }

        // ---- Highest category ----
        topCategory?.let { (cat, amt) ->
            val pct = (amt / totalSpent * 100).toInt()
            insights += Insight(
                "Your highest spending category is $cat ($pct% of total spending).",
                InsightType.INFO
            )
        }

        // ---- Savings tip ----
        if (budget != null && budget.monthlyLimit > 0 && !budget.limitExceeded) {
            val saveable = budget.remainingAmount * 0.5
            if (saveable > 100) {
                insights += Insight(
                    "Try saving ₹${"%.0f".format(saveable)} more this month!",
                    InsightType.TIP
                )
            }
        }

        // ---- Investment insight ----
        if (totalInvested > 0) {
            val investPct = if (totalSpent > 0) (totalInvested / totalSpent * 100).toInt() else 0
            insights += when {
                investPct >= 20 ->
                    Insight("Excellent! You're investing ${investPct}% of your spending. Keep it up!", InsightType.TIP)
                investPct >= 10 ->
                    Insight("Good habit — ₹${"%.0f".format(totalInvested)} invested so far this period.", InsightType.INFO)
                else ->
                    Insight("Consider increasing your SIP or FD contributions for long-term wealth.", InsightType.TIP)
            }
        } else {
            insights += Insight(
                "No investments tracked yet. Start a SIP or FD to grow your wealth.",
                InsightType.TIP
            )
        }

        // Sort: WARNING first, then TIP, then INFO. Return top 5.
        return insights
            .sortedBy { when (it.type) { InsightType.WARNING -> 0; InsightType.TIP -> 1; InsightType.INFO -> 2 } }
            .take(5)
    }
}
