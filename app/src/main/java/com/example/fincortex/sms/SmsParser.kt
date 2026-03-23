package com.example.fincortex.sms

import com.example.fincortex.model.TransactionModel
import com.example.fincortex.utils.MLCategoryClassifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * Advanced multi-format bank SMS parser.
 *
 * Supports four distinct banking channels with format-specific extraction:
 *  - UPI        – PhonePe / GPay / BHIM / any bank UPI VPA
 *  - Debit Card – POS / contactless / ATM withdrawals
 *  - Credit Card– purchase / EMI / bill payment
 *  - Net Banking– NEFT / RTGS / IMPS / fund transfer
 *
 * Coexists with [SmsReader]; both produce [TransactionModel] objects.
 * This class uses [MLCategoryClassifier] (scoring-based, includes Investment).
 */
object SmsParser {

    // ─────────────────────── Format Enum ─────────────────────────────────────

    enum class SmsFormat { UPI, DEBIT_CARD, CREDIT_CARD, NET_BANKING, UNKNOWN }

    data class ParseResult(
        val transaction: TransactionModel,
        val format: SmsFormat
    )

    // ─────────────────────── Shared Ignore List ───────────────────────────────

    private val IGNORE_KEYWORDS = listOf(
        "otp", "one time password", "verification code", "password",
        "reset", "login otp", "kyc", "suspicious activity", "new device"
    )

    // ─────────────────────── Amount Patterns ─────────────────────────────────

    private val AMOUNT_PATTERNS = listOf(
        // Rs. / Rs / RS 1,23,456.00
        Pattern.compile("Rs\\.?\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
        // INR 1234.00
        Pattern.compile("INR\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
        // ₹ 1234
        Pattern.compile("₹\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)"),
        // 5000 debited / 5000 credited
        Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)\\s*(?:debited|credited)", Pattern.CASE_INSENSITIVE),
        // amount of 500
        Pattern.compile("amount\\s+of\\s+(?:Rs\\.?\\s*)?(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE)
    )

    // ─────────────────────── UPI Patterns ────────────────────────────────────

    private val UPI_MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?:paid|sent|transferred)\\s+to\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+(?:via|on|using)|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("upi\\s+txn\\s+(?:at|to)\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+on|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vpa\\s*[:-]?\\s*([A-Za-z0-9._-]+@[A-Za-z0-9._-]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("upi[-/]([A-Za-z0-9 &._-]{2,30})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("at\\s+([A-Za-z0-9 &._'-]{2,40}?)\\s+on\\s+\\d", Pattern.CASE_INSENSITIVE)
    )

    // ─────────────────────── Card Patterns ───────────────────────────────────

    private val CARD_MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?:used|swiped)\\s+at\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+on|\\s+for|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("purchase\\s+at\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+on|\\s+for|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("transaction\\s+at\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+on|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("towards\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+on|\\s+via|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("at\\s+([A-Za-z0-9 &._'-]{2,40}?)\\s+on\\s+\\d", Pattern.CASE_INSENSITIVE)
    )

    // ─────────────────────── Net Banking Patterns ────────────────────────────

    private val NET_BANKING_PATTERNS = listOf(
        Pattern.compile("(?:to|towards)\\s+([A-Za-z0-9 &._'-]{2,40}?)\\s+(?:a/c|account|ref|on)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("beneficiary\\s*[:-]\\s*([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+(?:a/c|ref)|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("transferred\\s+to\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+(?:a/c|account)|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:neft|rtgs|imps)\\s+(?:to|cr)\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:[.,]|$)", Pattern.CASE_INSENSITIVE)
    )

    // ─────────────────────── Generic Fallback ────────────────────────────────

    private val GENERIC_PATTERNS = listOf(
        Pattern.compile("at\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+on|\\s+via|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("to\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+via|\\s+on|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("from\\s+([A-Za-z0-9 &._'-]{2,40}?)(?:\\s+via|\\s+on|[.,]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("merchant\\s*[:-]\\s*([A-Za-z0-9 &._'-]{2,40})", Pattern.CASE_INSENSITIVE)
    )

    // ─────────────────────── Public API ──────────────────────────────────────

    /** Detects which banking channel an SMS body belongs to. */
    fun detectFormat(body: String): SmsFormat {
        val lower = body.lowercase()
        return when {
            lower.contains("upi") || lower.contains("upi ref") ||
            lower.contains("upi id") || lower.contains("upi txn") ||
            lower.contains("@") && (lower.contains("paid") || lower.contains("sent")) ->
                SmsFormat.UPI

            (lower.contains("credit card") || lower.contains("credit card ending") ||
             lower.contains("cc no") || lower.contains("cc:")) ->
                SmsFormat.CREDIT_CARD

            (lower.contains("debit card") || lower.contains("debit card ending") ||
             lower.contains("atm") || lower.contains("pos") || lower.contains("card ending")) ->
                SmsFormat.DEBIT_CARD

            (lower.contains("neft") || lower.contains("rtgs") || lower.contains("imps") ||
             lower.contains("netbanking") || lower.contains("net banking") ||
             lower.contains("fund transfer")) ->
                SmsFormat.NET_BANKING

            else -> SmsFormat.UNKNOWN
        }
    }

    /**
     * Parses an SMS body into a [ParseResult].
     * Returns null if the message is an OTP or contains no recognisable amount.
     */
    fun parse(body: String, dateMillis: Long = System.currentTimeMillis()): ParseResult? {
        val lower = body.lowercase()
        if (IGNORE_KEYWORDS.any { lower.contains(it) }) return null

        val amount   = extractAmount(body) ?: return null
        val format   = detectFormat(body)
        val type     = detectTransactionType(lower)
        val merchant = when (format) {
            SmsFormat.UPI         -> extractWith(UPI_MERCHANT_PATTERNS, body)
            SmsFormat.DEBIT_CARD  -> extractWith(CARD_MERCHANT_PATTERNS, body)
            SmsFormat.CREDIT_CARD -> extractWith(CARD_MERCHANT_PATTERNS, body)
            SmsFormat.NET_BANKING -> extractWith(NET_BANKING_PATTERNS, body)
            SmsFormat.UNKNOWN     -> extractWith(GENERIC_PATTERNS, body)
        }.ifBlank { extractFallbackMerchant(body) }

        val category = MLCategoryClassifier.predictCategory("$merchant $body".take(300))
        val dateStr  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            .format(Date(dateMillis))

        val txn = TransactionModel(
            transactionId = "${format.name}_${dateMillis}_${amount.toLong()}",
            amount        = amount,
            merchant      = merchant,
            category      = category,
            date          = dateStr,
            type          = type,
            createdAt     = dateMillis,
            isFromSMS     = true,
            description   = body.take(200)
        )
        return ParseResult(txn, format)
    }

    // ─────────────────────── Private Helpers ─────────────────────────────────

    private fun extractAmount(body: String): Double? {
        for (pat in AMOUNT_PATTERNS) {
            val m = pat.matcher(body)
            if (m.find()) {
                val raw   = m.group(1)?.replace(",", "") ?: continue
                val value = raw.toDoubleOrNull() ?: continue
                if (value > 0) return value
            }
        }
        return null
    }

    private fun detectTransactionType(lower: String): String {
        val creditWords = listOf(
            "credited", "received", "deposit", "refund",
            "cashback", "salary", "reward", "reversal"
        )
        return if (creditWords.any { lower.contains(it) }) "credit" else "debit"
    }

    private fun extractWith(patterns: List<Pattern>, body: String): String {
        for (pat in patterns) {
            val m = pat.matcher(body)
            if (m.find()) {
                val candidate = m.group(1)?.trim() ?: continue
                if (candidate.length in 2..50) return candidate
            }
        }
        return ""
    }

    private fun extractFallbackMerchant(body: String): String =
        body.split(" ").take(3)
            .joinToString(" ")
            .replace(Regex("[^a-zA-Z0-9 ]"), "")
            .trim()
            .ifBlank { "Unknown" }
}
