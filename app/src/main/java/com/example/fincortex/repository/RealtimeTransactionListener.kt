package com.example.fincortex.repository

import com.example.fincortex.model.TransactionModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Attaches a Firestore [addSnapshotListener] to the "transactions" collection
 * for a given user and exposes live updates as a [StateFlow].
 *
 * Call [start] once after authentication and [stop] in ViewModel.onCleared().
 */
class RealtimeTransactionListener {

    private val db = FirebaseFirestore.getInstance()
    private var registration: ListenerRegistration? = null

    private val _transactions = MutableStateFlow<List<TransactionModel>>(emptyList())
    val transactions: StateFlow<List<TransactionModel>> = _transactions.asStateFlow()

    /** Begin listening for real-time updates for [userId]. Safe to call multiple times. */
    fun start(userId: String) {
        // Remove any existing listener before attaching a new one
        registration?.remove()

        registration = db.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents
                    .mapNotNull { it.toObject(TransactionModel::class.java) }
                    .sortedByDescending { it.createdAt }

                _transactions.value = list
            }
    }

    /** Detach the Firestore listener and clear the cached list. */
    fun stop() {
        registration?.remove()
        registration = null
        _transactions.value = emptyList()
    }
}
