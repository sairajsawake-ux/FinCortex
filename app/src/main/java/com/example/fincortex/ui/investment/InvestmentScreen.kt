package com.example.fincortex.ui.investment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fincortex.model.InvestmentModel
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkSecondary
import com.example.fincortex.ui.theme.DarkText
import com.example.fincortex.viewmodel.InvestmentViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val INVESTMENT_TYPES = listOf(
    "Stocks", "Mutual Fund", "SIP", "Fixed Deposit", "Crypto", "Gold", "NPS", "PPF", "Others"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(navController: NavController) {
    val vm: InvestmentViewModel = viewModel()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) vm.loadInvestments(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investments", color = DarkText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DarkAccent,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Investment")
            }
        },
        containerColor = DarkBackground
    ) { padding ->

        if (vm.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Total invested summary card
                item { TotalInvestmentCard(vm.totalInvested) }

                // Type breakdown
                if (vm.typeSummary.isNotEmpty()) {
                    item { TypeBreakdownCard(vm.typeSummary) }
                }

                // Investment list header
                if (vm.investments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Portfolio  (${vm.investments.size} entries)",
                            color = DarkText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(vm.investments) { inv ->
                        InvestmentRow(
                            investment = inv,
                            onDelete = { vm.deleteInvestment(inv.investmentId, userId) }
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = DarkAccent,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No investments yet.",
                                    color = DarkText.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Tap + to add your first entry.",
                                    color = DarkText.copy(alpha = 0.4f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }

    if (showAddDialog) {
        AddInvestmentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { inv ->
                vm.addInvestment(inv, userId) { showAddDialog = false }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TotalInvestmentCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Invested",
                color = DarkText.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${"%.2f".format(total)}",
                color = DarkAccent,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TypeBreakdownCard(summary: Map<String, Double>) {
    val total = summary.values.sum().coerceAtLeast(1.0)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "By Type",
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            summary.entries.sortedByDescending { it.value }.forEach { (type, amt) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(DarkAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type, color = DarkText, fontSize = 13.sp)
                    }
                    Text(
                        "₹${"%.0f".format(amt)}  (${(amt / total * 100).toInt()}%)",
                        color = DarkAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Investment row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InvestmentRow(investment: InvestmentModel, onDelete: () -> Unit) {
    val returnColor = when {
        investment.returns > 0 -> Color.Green
        investment.returns < 0 -> Color.Red
        else -> DarkText.copy(alpha = 0.6f)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSecondary)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = investment.name.ifBlank { "Unnamed" },
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = investment.type,
                    color = DarkAccent.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                if (investment.date.isNotBlank()) {
                    Text(
                        text = investment.date,
                        color = DarkText.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${"%.2f".format(investment.amount)}",
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (investment.returns != 0.0) {
                    val sign = if (investment.returns > 0) "+" else ""
                    Text(
                        text = "$sign₹${"%.2f".format(investment.returns)}",
                        color = returnColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Investment Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInvestmentDialog(
    onDismiss: () -> Unit,
    onAdd: (InvestmentModel) -> Unit
) {
    var name    by remember { mutableStateOf("") }
    var amount  by remember { mutableStateOf("") }
    var returns by remember { mutableStateOf("") }
    var notes   by remember { mutableStateOf("") }
    var selectedType  by remember { mutableStateOf(INVESTMENT_TYPES.first()) }
    var typeExpanded  by remember { mutableStateOf(false) }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = DarkAccent,
        unfocusedBorderColor = DarkText.copy(alpha = 0.4f),
        focusedLabelColor    = DarkAccent,
        unfocusedLabelColor  = DarkText.copy(alpha = 0.6f),
        cursorColor          = DarkAccent,
        focusedTextColor     = DarkText,
        unfocusedTextColor   = DarkText
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkPrimary,
        title = {
            Text("Add Investment", color = DarkText, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Nifty 50 SIP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(DarkPrimary)
                    ) {
                        INVESTMENT_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = DarkText) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = returns,
                    onValueChange = { returns = it },
                    label = { Text("Returns / Current Value (₹, optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@TextButton
                    onAdd(
                        InvestmentModel(
                            name    = name.trim(),
                            amount  = amt,
                            type    = selectedType,
                            returns = returns.toDoubleOrNull() ?: 0.0,
                            date    = todayStr,
                            notes   = notes.trim()
                        )
                    )
                }
            ) { Text("Add", color = DarkAccent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkText.copy(alpha = 0.7f))
            }
        }
    )
}
