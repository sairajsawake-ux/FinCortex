package com.example.fincortex.repository

import com.example.fincortex.model.InvestmentModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handles Firestore CRUD for the "investments" collection.
 * Every document is scoped to a [userId] so multi-user data is fully isolated.
 */
class InvestmentRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("investments")

    /** Returns all investments for [userId], sorted newest first. */
    suspend fun getInvestments(userId: String): List<InvestmentModel> =
        suspendCancellableCoroutine { cont ->
            col.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snap ->
                    val list = snap.documents
                        .mapNotNull { it.toObject(InvestmentModel::class.java) }
                        .sortedByDescending { it.createdAt }
                    cont.resume(list)
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }

    /** Adds a new investment document; Firestore generates the document ID. */
    suspend fun addInvestment(investment: InvestmentModel): Boolean =
        suspendCancellableCoroutine { cont ->
            val docId = col.document().id
            col.document(docId)
                .set(investment.copy(investmentId = docId))
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    /** Deletes the investment document with the given [investmentId]. */
    suspend fun deleteInvestment(investmentId: String): Boolean =
        suspendCancellableCoroutine { cont ->
            col.document(investmentId)
                .delete()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    /** Returns the sum of [InvestmentModel.amount] for [userId]. */
    suspend fun getTotalInvested(userId: String): Double =
        getInvestments(userId).sumOf { it.amount }

    /**
     * Returns a map of investment type → total amount for [userId].
     * Useful for a portfolio breakdown chart.
     */
    suspend fun getTypeSummary(userId: String): Map<String, Double> =
        getInvestments(userId)
            .groupBy { it.type }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
}
