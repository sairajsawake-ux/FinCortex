package com.example.fincortex.repository

import com.example.fincortex.model.TransactionModel
import com.example.fincortex.utils.MLCategoryClassifier
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handles Firestore CRUD operations for the "transactions" collection.
 */
class TransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("transactions")

    /**
     * Add a transaction document to Firestore.
     * Runs [MLCategoryClassifier] on merchant + description before writing so
     * every saved transaction has the best available category.
     */
    suspend fun addTransaction(transaction: TransactionModel): Boolean {
        val mlCategory = MLCategoryClassifier.predictCategory(
            "${transaction.merchant} ${transaction.description}"
        )
        // Prefer the ML prediction; only keep the incoming category when ML
        // cannot improve on it (i.e. both agree on "Others").
        val finalCategory = if (mlCategory != "Others") mlCategory else transaction.category

        return suspendCancellableCoroutine { cont ->
            val docId = col.document().id
            col.document(docId)
                .set(transaction.copy(transactionId = docId, category = finalCategory))
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }
    }

    /** Fetch all transactions for a user, sorted newest first. */
    suspend fun getTransactions(userId: String): List<TransactionModel> =
        suspendCancellableCoroutine { cont ->
            col.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents
                        .mapNotNull { it.toObject(TransactionModel::class.java) }
                        .sortedByDescending { it.createdAt }
                    cont.resume(list)
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }

    /**
     * Returns a map of category -> total debit amount for a user.
     * Computed client-side to avoid Firestore composite index requirements.
     */
    suspend fun getCategorySummary(userId: String): Map<String, Double> {
        val transactions = getTransactions(userId)
        return transactions
            .filter { it.type == "debit" }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }

    /** Returns total debit amount for the current calendar month. */
    suspend fun getMonthlySpent(userId: String): Double {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val monthStartMillis = cal.timeInMillis
        val transactions = getTransactions(userId)
        return transactions
            .filter { it.type == "debit" && it.createdAt >= monthStartMillis }
            .sumOf { it.amount }
    }
}
