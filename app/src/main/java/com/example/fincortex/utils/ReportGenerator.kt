package com.example.fincortex.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.fincortex.model.TransactionModel
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Generates and shares financial reports in CSV or PDF format.
 *
 * Files are written to the app's external Documents directory
 * (accessible without WRITE_EXTERNAL_STORAGE on API 29+) and shared
 * via a [FileProvider] declared in AndroidManifest.xml.
 */
object ReportGenerator {

    private val fileDateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val rowDateFmt  = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // -------------------------------------------------------------------------
    // CSV
    // -------------------------------------------------------------------------

    /**
     * Writes all [transactions] to a CSV file and returns its [Uri],
     * or null if an error occurs.
     */
    fun exportCsv(context: Context, transactions: List<TransactionModel>): Uri? = runCatching {
        val fileName = "FinCortex_Transactions_${fileDateFmt.format(Date())}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        FileWriter(file).use { w ->
            w.append("Transaction ID,Date,Merchant,Category,Type,Amount\n")
            transactions.forEach { txn ->
                val merchant = txn.merchant.replace("\"", "\"\"") // CSV-escape quotes
                w.append("${txn.transactionId},")
                w.append("${rowDateFmt.format(Date(txn.createdAt))},")
                w.append("\"$merchant\",")
                w.append("${txn.category},")
                w.append("${txn.type},")
                w.append("${txn.amount}\n")
            }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }.getOrNull()

    /** Triggers the system share sheet for the generated CSV file. */
    fun shareCsv(context: Context, transactions: List<TransactionModel>) {
        val uri = exportCsv(context, transactions) ?: return
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "FinCortex Transaction Report")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share CSV Report"
            )
        )
    }

    // -------------------------------------------------------------------------
    // PDF — A4 monthly summary
    // -------------------------------------------------------------------------

    /**
     * Generates an A4 PDF for the current calendar month and returns its [Uri],
     * or null if an error occurs.
     */
    fun exportPdf(
        context: Context,
        transactions: List<TransactionModel>,
        monthlyLimit: Double = 0.0
    ): Uri? = runCatching {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthLabel  = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        val monthly     = transactions.filter { it.createdAt >= monthStart }
        val debits      = monthly.filter { it.type == "debit" }
        val credits     = monthly.filter { it.type == "credit" }
        val totalDebits = debits.sumOf { it.amount }
        val totalCredits= credits.sumOf { it.amount }
        val categoryMap = debits
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }

        // ---- Paints ----
        val titlePaint = Paint().apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.parseColor("#1F2E3D")
        }
        val headPaint = Paint().apply {
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.parseColor("#B8860B") // dark gold
        }
        val bodyPaint = Paint().apply { textSize = 11f; color = Color.BLACK }
        val subPaint  = Paint().apply { textSize = 10f; color = Color.DKGRAY }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.8f }

        // ---- Build page ----
        val document = PdfDocument()
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(595, 842, 1).create()  // A4 portrait
        )
        val c: Canvas = page.canvas
        var y = 56f

        // Title + subtitle
        c.drawText("FinCortex — Monthly Report", 40f, y, titlePaint); y += 10f
        c.drawLine(40f, y, 555f, y, linePaint);                        y += 18f
        c.drawText("Period: $monthLabel", 40f, y, subPaint);           y += 22f

        // Summary
        c.drawText("SUMMARY", 40f, y, headPaint); y += 16f
        c.drawText("Total Spent:     ₹${"%.2f".format(totalDebits)}", 40f, y, bodyPaint); y += 16f
        c.drawText("Total Received:  ₹${"%.2f".format(totalCredits)}", 40f, y, bodyPaint); y += 16f
        if (monthlyLimit > 0) {
            val status = if (totalDebits > monthlyLimit) "EXCEEDED" else "within limit"
            c.drawText(
                "Monthly Budget:  ₹${"%.2f".format(monthlyLimit)}  ($status)",
                40f, y, bodyPaint
            )
            y += 16f
        }
        c.drawText("Transactions:    ${monthly.size} total  " +
                "(${debits.size} debits, ${credits.size} credits)", 40f, y, bodyPaint)
        y += 22f

        // Category breakdown
        c.drawLine(40f, y, 555f, y, linePaint); y += 14f
        c.drawText("SPENDING BY CATEGORY", 40f, y, headPaint); y += 16f
        categoryMap.forEach { (cat, amt) ->
            if (y > 760f) return@forEach
            c.drawText(cat, 52f, y, bodyPaint)
            c.drawText("₹${"%.2f".format(amt)}", 420f, y, bodyPaint)
            y += 14f
        }
        y += 8f

        // Transaction table
        c.drawLine(40f, y, 555f, y, linePaint); y += 14f
        c.drawText("TRANSACTIONS  (showing up to 30 most recent)", 40f, y, headPaint); y += 16f

        // Table header
        val col = floatArrayOf(40f, 115f, 280f, 390f, 470f)
        arrayOf("Date", "Merchant", "Category", "Type", "Amount").forEachIndexed { i, h ->
            c.drawText(h, col[i], y, subPaint)
        }
        y += 6f; c.drawLine(40f, y, 555f, y, linePaint); y += 12f

        val shortDate = SimpleDateFormat("dd MMM", Locale.getDefault())
        monthly.sortedByDescending { it.createdAt }.take(30).forEach { txn ->
            if (y > 810f) return@forEach
            val sign = if (txn.type == "credit") "+" else "-"
            c.drawText(shortDate.format(Date(txn.createdAt)), col[0], y, bodyPaint)
            c.drawText(txn.merchant.take(20), col[1], y, bodyPaint)
            c.drawText(txn.category, col[2], y, bodyPaint)
            c.drawText(txn.type, col[3], y, bodyPaint)
            c.drawText("${sign}₹${"%.2f".format(txn.amount)}", col[4], y, bodyPaint)
            y += 13f
        }

        document.finishPage(page)

        // Write to file
        val fileName = "FinCortex_${monthLabel.replace(" ", "_")}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }.getOrNull()

    /** Triggers the system share sheet for the generated PDF file. */
    fun sharePdf(
        context: Context,
        transactions: List<TransactionModel>,
        monthlyLimit: Double = 0.0
    ) {
        val uri = exportPdf(context, transactions, monthlyLimit) ?: return
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "FinCortex Monthly Report")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share PDF Report"
            )
        )
    }
}
