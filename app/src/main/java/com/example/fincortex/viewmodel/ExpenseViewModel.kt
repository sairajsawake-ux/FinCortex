package com.example.fincortex.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fincortex.model.Expense
import com.example.fincortex.repository.ExpenseRepository

class ExpenseViewModel : ViewModel() {

    private val repository = ExpenseRepository()

    var expenses by mutableStateOf<List<Expense>>(emptyList())
        private set

    fun loadExpenses(userId: String) {
        repository.getExpensesByUser(userId) {
            expenses = it
        }
    }

    fun addExpense(expense: Expense, onResult: (Boolean) -> Unit) {
        repository.addExpense(expense) { success ->
            onResult(success)
        }
    }
}
