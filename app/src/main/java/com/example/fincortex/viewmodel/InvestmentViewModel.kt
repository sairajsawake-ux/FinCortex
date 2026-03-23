package com.example.fincortex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fincortex.model.InvestmentModel
import com.example.fincortex.repository.InvestmentRepository
import kotlinx.coroutines.launch

class InvestmentViewModel : ViewModel() {

    private val repository = InvestmentRepository()

    var investments by mutableStateOf<List<InvestmentModel>>(emptyList())
        private set

    var totalInvested by mutableStateOf(0.0)
        private set

    var typeSummary by mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    var isLoading by mutableStateOf(false)
        private set

    /** Error message shown to the user, null when there is none. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadInvestments(userId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            investments  = repository.getInvestments(userId)
            totalInvested = investments.sumOf { it.amount }
            typeSummary   = investments
                .groupBy { it.type }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
            isLoading = false
        }
    }

    fun addInvestment(investment: InvestmentModel, userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.addInvestment(investment.copy(userId = userId))
            if (success) loadInvestments(userId)
            onResult(success)
        }
    }

    fun deleteInvestment(investmentId: String, userId: String) {
        viewModelScope.launch {
            repository.deleteInvestment(investmentId)
            loadInvestments(userId)
        }
    }
}
