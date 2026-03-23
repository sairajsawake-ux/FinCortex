package com.example.fincortex.repository

import com.example.fincortex.model.BudgetModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handles Firestore CRUD operations for the "budgets" collection.
 */
class BudgetRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("budgets")

    /** Fetch the budget document for a user. Returns null if not set. */
    suspend fun getBudget(userId: String): BudgetModel? =
        suspendCancellableCoroutine { cont ->
            col.whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val budget = snapshot.documents.firstOrNull()
                        ?.toObject(BudgetModel::class.java)
                    cont.resume(budget)
                }
                .addOnFailureListener { cont.resume(null) }
        }

    /** Create or overwrite the budget document for a user (doc ID = userId). */
    suspend fun saveBudget(budget: BudgetModel): Boolean =
        suspendCancellableCoroutine { cont ->
            col.document(budget.userId)
                .set(budget)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    /** Update only the spent/remaining/exceeded fields (requires existing document). */
    suspend fun updateSpent(userId: String, spent: Double, limit: Double): Boolean {
        val remaining = (limit - spent).coerceAtLeast(0.0)
        val exceeded = spent > limit
        val updates = mapOf(
            "spentAmount" to spent,
            "remainingAmount" to remaining,
            "limitExceeded" to exceeded,
            "updatedAt" to System.currentTimeMillis()
        )
        return suspendCancellableCoroutine { cont ->
            col.document(userId)
                .update(updates)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }
    }
}
