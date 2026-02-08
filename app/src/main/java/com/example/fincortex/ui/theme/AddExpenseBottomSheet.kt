package com.example.fincortex.ui.theme

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fincortex.model.ApiResponse
import com.example.fincortex.model.ExpenseRequest
import com.example.fincortex.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    onAdd: (ExpenseRequest) -> Unit,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun addExpense() {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null && title.isNotBlank() && amount.isNotBlank()) {
            val expenseRequest = ExpenseRequest(amount.toInt(), title)
            val call = RetrofitClient.apiService.addExpense("Bearer $token", expenseRequest)

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        onAdd(expenseRequest)
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // Handle failure
                }
            })
        }
    }

    ModalBottomSheet(
        onDismissRequest = onClose
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    addExpense()
                    onClose()
                }
            ) {
                Text("Add Expense")
            }
        }
    }
}
