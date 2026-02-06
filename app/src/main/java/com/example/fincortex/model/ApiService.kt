package com.yourpackage.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("ai/insights")
    suspend fun getInsights(
        @Body request: ExpenseRequest
    ): Response<InsightResponse>
}
