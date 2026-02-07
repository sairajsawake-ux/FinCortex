package com.example.fincortex.network

import com.example.fincortex.model.ApiResponse
import com.example.fincortex.model.ExpenseRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("/expense/add")
    fun addExpense(
        @Header("Authorization") token: String,
        @Body request: ExpenseRequest
    ): Call<ApiResponse>
}
