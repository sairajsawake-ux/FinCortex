package com.example.fincortex.repository

import com.example.fincortex.model.Expense

class ExpenseRepository {

    private val expenses = mutableListOf<Expense>()
    private var nextId = 1

    fun addExpense(name: String, amount: Double) {
        if (name.isBlank()) return

        expenses.add(
            Expense(
                id = nextId,
                name = name,
                amount = amount
            )
        )
        nextId++
    }

    fun deleteExpense(id: Int) {
        expenses.removeAll { it.id == id }
    }

    fun getAllExpenses(): List<Expense> = expenses
}
