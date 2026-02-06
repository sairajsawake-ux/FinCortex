package com.example.fincortex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fincortex.model.Expense
import com.example.fincortex.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseViewModel : ViewModel() {

    private val repository = ExpenseRepository()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    fun addExpense(name: String, amountText: String) {
        val amount = amountText.toDoubleOrNull() ?: return
        repository.addExpense(name, amount)
        _expenses.value = repository.getAllExpenses()
    }

    fun deleteExpense(id: Int) {
        repository.deleteExpense(id)
        _expenses.value = repository.getAllExpenses()
    }
}
