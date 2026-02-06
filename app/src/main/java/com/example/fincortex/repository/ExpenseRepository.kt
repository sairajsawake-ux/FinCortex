package com.example.fincortex.repository

import com.example.fincortex.model.Expense
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val expensesCollection = db.collection("expenses")

    fun addExpense(expense: Expense, onResult: (Boolean) -> Unit) {
        val docId = expensesCollection.document().id
        expensesCollection
            .document(docId)
            .set(expense.copy(id = docId))
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun getExpensesByUser(
        userId: String,
        onResult: (List<Expense>) -> Unit
    ) {
        expensesCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.documents.mapNotNull {
                    it.toObject(Expense::class.java)
                }
                onResult(expenses)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
