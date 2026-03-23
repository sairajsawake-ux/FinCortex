package com.example.fincortex.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fincortex.NotificationService
import com.example.fincortex.R

/**
 * Centralised notification helper for FinCortex.
 *
 * Provides targeted alerts for:
 *  - Budget exceeded / near-limit warning
 *  - High daily spending
 *  - New transaction recorded
 *
 * Reuses [NotificationService.CHANNEL_ID] so all notifications appear
 * in the same system channel. The channel is created lazily on first use;
 * no change to [FinCortexApp] is required.
 *
 * Note: On Android 13+ (API 33) the caller must hold
 * POST_NOTIFICATIONS permission before calling any show* method.
 * The permission is already declared in AndroidManifest.xml.
 */
object NotificationHelper {

    private const val ID_BUDGET_EXCEEDED  = 200
    private const val ID_BUDGET_WARNING   = 201
    private const val ID_DAILY_HIGH       = 202
    private const val ID_NEW_TRANSACTION  = 203
    private const val ID_UNUSUAL_TXN      = 204

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Call when [spent] has surpassed [limit]. Shows a high-priority alert. */
    fun showBudgetExceeded(context: Context, spent: Double, limit: Double) {
        show(
            context = context,
            id = ID_BUDGET_EXCEEDED,
            title = "Budget Exceeded!",
            body = "You spent ₹${"%.0f".format(spent)} against your " +
                    "₹${"%.0f".format(limit)} monthly limit.",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /** Call when the user reaches [pct]% of their monthly budget. */
    fun showBudgetWarning(context: Context, pct: Int) {
        show(
            context = context,
            id = ID_BUDGET_WARNING,
            title = "Budget Alert",
            body = "You've used $pct% of your monthly budget.",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    /** Call when today's spend looks unusually high. */
    fun showDailyHighSpending(context: Context, amount: Double) {
        show(
            context = context,
            id = ID_DAILY_HIGH,
            title = "High Daily Spending",
            body = "You've spent ₹${"%.0f".format(amount)} today — more than usual.",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    /** Call when a single debit is significantly above the user's average. */
    fun showUnusualTransaction(context: Context, merchant: String, amount: Double, avg: Double) {
        show(
            context = context,
            id = ID_UNUSUAL_TXN,
            title = "Unusual Transaction Detected",
            body = "₹${"%.0f".format(amount)} at ${merchant.ifBlank { "Unknown" }} is " +
                    "${(amount / avg).toInt()}× your average debit of ₹${"%.0f".format(avg)}.",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /** Call after a new transaction is successfully saved. */
    fun showNewTransaction(context: Context, merchant: String, amount: Double, type: String) {
        val sign = if (type == "credit") "+" else "-"
        show(
            context = context,
            id = ID_NEW_TRANSACTION,
            title = "New Transaction",
            body = "${sign}₹${"%.0f".format(amount)} recorded at ${merchant.ifBlank { "Unknown" }}.",
            priority = NotificationCompat.PRIORITY_LOW
        )
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun show(
        context: Context,
        id: Int,
        title: String,
        body: String,
        priority: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        ensureChannelExists(manager)

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            Intent(Intent.ACTION_VIEW, Uri.parse("fincortex://activity_hub")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.fincortex_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    private fun ensureChannelExists(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(NotificationService.CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                NotificationService.CHANNEL_ID,
                "FinCortex Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Budget alerts and transaction notifications"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
