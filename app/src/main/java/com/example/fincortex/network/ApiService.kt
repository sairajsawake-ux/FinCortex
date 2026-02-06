package com.example.fincortex.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("get_insights")
    suspend fun getInsights(@Body request: ExpenseRequest): Response<InsightResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>
}
