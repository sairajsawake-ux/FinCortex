package com.example.fincortex.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fincortex.model.Expense
import com.example.fincortex.network.ExpenseDto
import com.example.fincortex.network.ExpenseRequest
import com.example.fincortex.network.InsightResponse
import com.example.fincortex.network.RetrofitClient
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {

    // ----------------------------
    // Expense List (already in your app)
    // ----------------------------
    private val _expenses = mutableStateOf<List<Expense>>(emptyList())
    val expenses: List<Expense> get() = _expenses.value

    fun addExpense(expense: Expense) {
        _expenses.value = _expenses.value + expense
        fetchInsights() // re-calc AI insight whenever expense changes
    }

    // ----------------------------
    // AI Insight State
    // ----------------------------
    private val _aiInsight = mutableStateOf<InsightResponse?>(null)
    val aiInsight: State<InsightResponse?> = _aiInsight

    // ----------------------------
    // Fetch AI Insights from Backend
    // ----------------------------
    fun fetchInsights() {
        viewModelScope.launch {
            try {
                // Convert app expenses → backend DTOs
                val expenseDtos = _expenses.value.map {
                    ExpenseDto(
                        amount = it.amount,
                        category = it.category
                    )
                }

                // Call backend API
                val response = RetrofitClient.api.getInsights(
                    ExpenseRequest(expenses = expenseDtos)
                )

                if (response.isSuccessful) {
                    _aiInsight.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
