package com.example.fincortex.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fincortex.viewmodel.ExpenseViewModel

@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val expenses by viewModel.expenses.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Expense name") }
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )

        Button(
            onClick = {
                viewModel.addExpense(name, amount)
                name = ""
                amount = ""
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(expenses) { expense ->
                ExpenseItem(
                    expense.name,
                    expense.amount,
                    onDelete = { viewModel.deleteExpense(expense.id) }
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    name: String,
    amount: Double,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = name, fontSize = 16.sp)
            Text(text = "₹$amount", fontSize = 14.sp)
        }
        TextButton(onClick = onDelete) {
            Text("Delete")
        }
    }
}
