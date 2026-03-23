package com.example.fincortex.sms

import android.content.Context
import android.net.Uri
import com.example.fincortex.model.TransactionModel
import com.example.fincortex.utils.CategoryUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * Reads and parses bank transaction SMS messages from the device inbox.
 * Requires READ_SMS permission.
 */
object SmsReader {

    // Known bank sender IDs
    private val BANK_SENDERS = listOf(
        "SBI", "HDFC", "ICICI", "AXIS", "KOTAK", "PNB", "BOB", "CANARA",
        "UNION", "INDUSIND", "YES", "FEDERAL", "IDFC", "RBL", "BANDHAN",
        "PAYTM", "PHONEPE", "GPAY", "RAZORPAY", "AIRTEL", "JIO",
        "YESBNK", "AUBANK", "HDFCBK", "ICICIB", "SBIINB", "VK-", "AD-"
    )

    // Keywords that confirm a message is a financial transaction
    private val TRANSACTION_KEYWORDS = listOf(
        "debited", "credited", "paid", "payment", "purchase",
        "withdrawn", "transfer", "upi", "rs.", "rs ", "inr", "₹"
    )

    // OTP and non-transaction keywords to skip
    private val IGNORE_KEYWORDS = listOf(
        "otp", "one time password", "verification code", "password",
        "reset password", "login otp", "new device", "login alert",
        "kyc", "welcome", "account activated", "suspicious activity"
    )

    // Regex patterns to extract amount (supports Rs., INR, ₹)
    private val AMOUNT_PATTERNS = listOf(
        Pattern.compile("Rs\\.?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("INR\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("₹\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)"),
        Pattern.compile("(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)\\s*debited", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)\\s*credited", Pattern.CASE_INSENSITIVE)
    )

    // Regex patterns to extract merchant name
    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("at\\s+([A-Za-z0-9 &.]+?)(?:\\s+on|\\s+via|\\.|,|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("to\\s+([A-Za-z0-9 &.]+?)(?:\\s+via|\\s+on|\\.|,|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("from\\s+([A-Za-z0-9 &.]+?)(?:\\s+via|\\s+on|\\.|,|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("merchant\\s*:\\s*([A-Za-z0-9 &.]+)", Pattern.CASE_INSENSITIVE)
    )

    /**
     * Reads all SMS from the inbox and returns parsed bank transactions.
     * Call only after READ_SMS permission is granted.
     */
    fun readBankSms(context: Context): List<TransactionModel> {
        val transactions = mutableListOf<TransactionModel>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")

        try {
            val cursor = context.contentResolver.query(
                uri, projection, null, null, "date DESC"
            )
            cursor?.use {
                val bodyIdx = it.getColumnIndex("body")
                val addrIdx = it.getColumnIndex("address")
                val dateIdx = it.getColumnIndex("date")

                while (it.moveToNext()) {
                    val body = it.getString(bodyIdx) ?: continue
                    val sender = it.getString(addrIdx) ?: ""
                    val dateMillis = it.getLong(dateIdx)

                    if (isBankSms(sender, body)) {
                        parseSms(body, dateMillis)?.let { txn -> transactions.add(txn) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return transactions
    }

    /** Returns true if the SMS is from a bank and contains a transaction keyword. */
    private fun isBankSms(sender: String, body: String): Boolean {
        val senderUpper = sender.uppercase()
        val bodyLower = body.lowercase()
        val isBankSender = BANK_SENDERS.any { senderUpper.contains(it) }
        val hasTransaction = TRANSACTION_KEYWORDS.any { bodyLower.contains(it) }
        val shouldIgnore = IGNORE_KEYWORDS.any { bodyLower.contains(it) }
        return (isBankSender && hasTransaction) && !shouldIgnore
    }

    /**
     * Parses a single SMS body and returns a TransactionModel, or null if not a transaction.
     */
    fun parseSms(body: String, dateMillis: Long = System.currentTimeMillis()): TransactionModel? {
        val bodyLower = body.lowercase()
        if (IGNORE_KEYWORDS.any { bodyLower.contains(it) }) return null
        if (!TRANSACTION_KEYWORDS.any { bodyLower.contains(it) }) return null

        val amount = extractAmount(body) ?: return null
        val type = detectType(bodyLower)
        val merchant = extractMerchant(body)
        val category = CategoryUtils.getCategory(merchant)
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            .format(Date(dateMillis))

        return TransactionModel(
            transactionId = "SMS_${dateMillis}_${amount.toLong()}",
            amount = amount,
            merchant = merchant.ifBlank { "Unknown" },
            category = category,
            date = dateStr,
            type = type,
            createdAt = dateMillis,
            isFromSMS = true,
            description = body.take(200)
        )
    }

    private fun extractAmount(body: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val raw = matcher.group(1)?.replace(",", "") ?: continue
                val amount = raw.toDoubleOrNull() ?: continue
                if (amount > 0) return amount
            }
        }
        return null
    }

    private fun detectType(bodyLower: String): String {
        val creditWords = listOf("credited", "received", "deposit", "refund", "cashback", "reward", "salary")
        if (creditWords.any { bodyLower.contains(it) }) return "credit"
        return "debit"
    }

    private fun extractMerchant(body: String): String {
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim() ?: continue
                if (merchant.length in 2..50) return merchant
            }
        }
        // Fallback: first few words
        return body.split(" ").take(3).joinToString(" ")
            .replace(Regex("[^a-zA-Z0-9 ]"), "").trim()
    }
}
