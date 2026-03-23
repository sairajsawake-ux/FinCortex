package com.example.fincortex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fincortex.model.BudgetModel
import com.example.fincortex.repository.BudgetRepository
import kotlinx.coroutines.launch

class BudgetViewModel : ViewModel() {

    private val repository = BudgetRepository()

    var budget by mutableStateOf<BudgetModel?>(null)
        private set

    /** Non-null when the user is at ≥90% or has exceeded their monthly limit. */
    var budgetWarning by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadBudget(userId: String) {
        viewModelScope.launch {
            isLoading = true
            budget = repository.getBudget(userId)
            updateWarning()
            isLoading = false
        }
    }

    /** Save or update the monthly limit for a user. */
    fun setBudgetLimit(userId: String, limit: Double) {
        viewModelScope.launch {
            val current = budget ?: BudgetModel(userId = userId)
            val updated = current.copy(
                userId = userId,
                monthlyLimit = limit,
                remainingAmount = (limit - current.spentAmount).coerceAtLeast(0.0),
                limitExceeded = current.spentAmount > limit,
                updatedAt = System.currentTimeMillis()
            )
            if (repository.saveBudget(updated)) {
                budget = updated
                updateWarning()
            }
        }
    }

    /**
     * Called after transactions are loaded to keep spentAmount in sync with
     * the actual monthly debit total from Firestore.
     */
    fun syncSpent(userId: String, spent: Double) {
        viewModelScope.launch {
            val limit = budget?.monthlyLimit ?: return@launch
            repository.updateSpent(userId, spent, limit)
            budget = budget?.copy(
                spentAmount = spent,
                remainingAmount = (limit - spent).coerceAtLeast(0.0),
                limitExceeded = spent > limit,
                updatedAt = System.currentTimeMillis()
            )
            updateWarning()
        }
    }

    private fun updateWarning() {
        val b = budget ?: run { budgetWarning = null; return }
        budgetWarning = when {
            b.monthlyLimit <= 0 -> null
            b.limitExceeded ->
                "Budget exceeded! Spent ₹${"%.2f".format(b.spentAmount)} of ₹${"%.2f".format(b.monthlyLimit)}"
            (b.spentAmount / b.monthlyLimit) >= 0.9 ->
                "Warning: ${(b.spentAmount / b.monthlyLimit * 100).toInt()}% of budget used"
            else -> null
        }
    }
}
