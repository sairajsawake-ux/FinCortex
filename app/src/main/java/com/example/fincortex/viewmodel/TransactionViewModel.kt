package com.example.fincortex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fincortex.model.TransactionModel
import com.example.fincortex.repository.RealtimeTransactionListener
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel : ViewModel() {

    private val realtimeListener = RealtimeTransactionListener()

    var transactions by mutableStateOf<List<TransactionModel>>(emptyList())
        private set

    var monthlySpent by mutableStateOf(0.0)
        private set

    var totalSpent by mutableStateOf(0.0)
        private set

    var categorySummary by mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    var isLoading by mutableStateOf(false)
        private set

    /**
     * Attaches the Firestore real-time listener for [userId] and starts
     * collecting updates. Derived stats (monthlySpent, totalSpent,
     * categorySummary) are recomputed automatically on every Firestore change.
     */
    fun loadDashboardData(userId: String) {
        isLoading = true
        realtimeListener.start(userId)

        viewModelScope.launch {
            realtimeListener.transactions.collect { list ->
                transactions = list
                recomputeStats(list)
                isLoading = false
            }
        }
    }

    private fun recomputeStats(list: List<TransactionModel>) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStart = cal.timeInMillis
        val debits = list.filter { it.type == "debit" }

        totalSpent = debits.sumOf { it.amount }
        monthlySpent = debits.filter { it.createdAt >= monthStart }.sumOf { it.amount }
        categorySummary = debits
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeListener.stop()
    }
}
