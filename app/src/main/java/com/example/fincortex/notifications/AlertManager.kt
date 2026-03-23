package com.example.fincortex.notifications

import android.content.Context
import android.content.SharedPreferences
import com.example.fincortex.model.BudgetModel
import com.example.fincortex.model.TransactionModel
import com.example.fincortex.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Monitors transaction / budget state and fires [NotificationHelper] alerts
 * when thresholds are crossed.
 *
 * State is persisted in SharedPreferences so the same alert is never shown
 * twice within the same day or budget cycle. Call [checkAndAlert] after
 * every Firestore data refresh (e.g. from a LaunchedEffect in the dashboard).
 *
 * Automatic triggers:
 *  - Budget exceeded             → high-priority notification
 *  - Budget ≥ 80 %               → warning notification (once per day)
 *  - Today's spending high        → if ≥ 3 debits or daily total > dailyThreshold
 *  - Unusual transaction          → single debit > 2× average debit amount
 */
object AlertManager {

    private const val PREFS_NAME = "fincortex_alert_prefs"

    // SharedPreference keys
    private const val KEY_BUDGET_EXCEEDED_ALERTED = "budget_exceeded_alerted"
    private const val KEY_BUDGET_WARN_DATE         = "budget_warn_date"
    private const val KEY_DAILY_HIGH_DATE          = "daily_high_date"
    private const val KEY_UNUSUAL_TXN_ID           = "unusual_txn_id"

    // ─────────────────────────── Public entry point ──────────────────────────

    /**
     * Evaluates current spending state and fires any outstanding alerts.
     *
     * @param context           Application / Activity context
     * @param transactions      Full transaction list for the current user
     * @param budget            User's monthly budget, or null if not set
     * @param dailyThreshold    Alert when daily spend exceeds this amount (default ₹2000)
     */
    fun checkAndAlert(
        context: Context,
        transactions: List<TransactionModel>,
        budget: BudgetModel?,
        dailyThreshold: Double = 2000.0
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayString()

        val debits = transactions.filter { it.type == "debit" }
        if (debits.isEmpty()) return

        // ── 1. Budget exceeded ───────────────────────────────────────────────
        if (budget != null && budget.monthlyLimit > 0 && budget.limitExceeded) {
            // Alert only once per budget period (reset at month start)
            val alerted = prefs.getBoolean(KEY_BUDGET_EXCEEDED_ALERTED, false)
            if (!alerted) {
                NotificationHelper.showBudgetExceeded(
                    context, budget.spentAmount, budget.monthlyLimit
                )
                prefs.edit().putBoolean(KEY_BUDGET_EXCEEDED_ALERTED, true).apply()
            }
        } else {
            // Reset flag at start of new month
            prefs.edit().remove(KEY_BUDGET_EXCEEDED_ALERTED).apply()
        }

        // ── 2. Budget warning at ≥ 80 % (once per day) ──────────────────────
        if (budget != null && budget.monthlyLimit > 0 && !budget.limitExceeded) {
            val usagePct = (budget.spentAmount / budget.monthlyLimit * 100).toInt()
            if (usagePct >= 80) {
                val lastWarnDate = prefs.getString(KEY_BUDGET_WARN_DATE, "")
                if (lastWarnDate != today) {
                    NotificationHelper.showBudgetWarning(context, usagePct)
                    prefs.edit().putString(KEY_BUDGET_WARN_DATE, today).apply()
                }
            }
        }

        // ── 3. High daily spending (once per day) ────────────────────────────
        val todayMillis = todayStartMillis()
        val todayDebits = debits.filter { it.createdAt >= todayMillis }
        val todayTotal  = todayDebits.sumOf { it.amount }

        if (todayDebits.size >= 3 || todayTotal > dailyThreshold) {
            val lastDailyDate = prefs.getString(KEY_DAILY_HIGH_DATE, "")
            if (lastDailyDate != today) {
                NotificationHelper.showDailyHighSpending(context, todayTotal)
                prefs.edit().putString(KEY_DAILY_HIGH_DATE, today).apply()
            }
        }

        // ── 4. Unusual transaction (amount > 2× average) ────────────────────
        val avgDebit = debits.sumOf { it.amount } / debits.size
        val latestTxn = debits.maxByOrNull { it.createdAt }
        if (latestTxn != null && latestTxn.amount > avgDebit * 2.0 && avgDebit > 100.0) {
            val lastAlertedId = prefs.getString(KEY_UNUSUAL_TXN_ID, "")
            if (lastAlertedId != latestTxn.transactionId) {
                NotificationHelper.showUnusualTransaction(
                    context,
                    latestTxn.merchant,
                    latestTxn.amount,
                    avgDebit
                )
                prefs.edit().putString(KEY_UNUSUAL_TXN_ID, latestTxn.transactionId).apply()
            }
        }
    }

    // ─────────────────────────── Helpers ─────────────────────────────────────

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun todayStartMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
