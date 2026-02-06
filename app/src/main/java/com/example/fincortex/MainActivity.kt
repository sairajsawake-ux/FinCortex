package com.example.fincortex

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import com.example.fincortex.network.ApiResponse
import com.example.fincortex.network.LoginRequest
import com.example.fincortex.network.RetrofitClient
import com.example.fincortex.ui.theme.FinCortexTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinCortexTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Text(text = "FinCortex Backend Connected")

                    // 🔥 SAFE: called once, no memory loop
                    LaunchedEffect(Unit) {
                        callLoginApi()
                    }
                }
            }
        }
    }

    private fun callLoginApi() {
        val apiService = RetrofitClient.api

        val request = LoginRequest(
            email = "test@gmail.com",
            password = "1234"
        )

        apiService.login(request)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    Log.d("API", "SUCCESS: ${response.body()?.message}")
                }

                override fun onFailure(
                    call: Call<ApiResponse>,
                    t: Throwable
                ) {
                    Log.e("API", "ERROR: ${t.message}")
                }
            })
    }
}
