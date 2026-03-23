package com.example.fincortex.ui.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fincortex.model.TransactionModel
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkSecondary
import com.example.fincortex.ui.theme.DarkText
import com.example.fincortex.notifications.AlertManager
import com.example.fincortex.ui.navigation.Routes
import com.example.fincortex.utils.AnalyticsUtils
import com.example.fincortex.utils.ChartHelper
import com.example.fincortex.utils.ReportGenerator
import com.example.fincortex.utils.SmartInsights
import com.example.fincortex.viewmodel.BudgetViewModel
import com.example.fincortex.viewmodel.InvestmentViewModel
import com.example.fincortex.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val transactionVM: TransactionViewModel = viewModel()
    val budgetVM: BudgetViewModel = viewModel()
    val investmentVM: InvestmentViewModel = viewModel()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            transactionVM.loadDashboardData(userId)
            budgetVM.loadBudget(userId)
            investmentVM.loadInvestments(userId)
        }
    }

    // Keep budget spent amount in sync with actual monthly spend
    LaunchedEffect(transactionVM.monthlySpent) {
        if (userId.isNotEmpty() && transactionVM.monthlySpent > 0) {
            budgetVM.syncSpent(userId, transactionVM.monthlySpent)
        }
    }

    // Automatic alerts — fires after every successful recomposition (non-Composable side effect)
    SideEffect {
        if (transactionVM.transactions.isNotEmpty()) {
            android.util.Log.d("DashboardScreen", "Checking alerts for ${transactionVM.transactions.size} transactions")
            AlertManager.checkAndAlert(context, transactionVM.transactions, budgetVM.budget)
        }
    }

    // Prepare chart data and insights outside the LazyColumn DSL
    val insights = remember(transactionVM.transactions, budgetVM.budget, investmentVM.totalInvested) {
        SmartInsights.generate(
            transactionVM.transactions,
            budgetVM.budget,
            investmentVM.totalInvested
        )
    }
    val categoryData = remember(transactionVM.categorySummary) {
        AnalyticsUtils.categoryChartData(transactionVM.transactions)
    }
    val monthlyData = remember(transactionVM.transactions) {
        AnalyticsUtils.monthlyChartData(transactionVM.transactions)
    }

    // Derived values for new sections
    val budgetLimit = budgetVM.budget?.monthlyLimit ?: 0.0
    val budgetRemaining = budgetVM.budget?.remainingAmount ?: 0.0
    val budgetExceeded = budgetVM.budget?.limitExceeded == true
    val topCategory = transactionVM.categorySummary.maxByOrNull { it.value }?.key ?: "N/A"
    val monthlyTxnCount = remember(transactionVM.transactions) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        transactionVM.transactions.count { it.createdAt >= cal.timeInMillis }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = DarkText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkPrimary)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (transactionVM.isLoading) {
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

                // ── DEBUG BANNER ─────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00C853), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "NEW DASHBOARD VERSION",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // ── HEADER ───────────────────────────────────────────────────
                item { DashboardHeader() }

                // ── BUDGET EXCEEDED ALERT ────────────────────────────────────
                if (budgetExceeded) {
                    item { BudgetExceededAlert() }
                }

                // ── SUMMARY CARDS ────────────────────────────────────────────
                item {
                    SummaryCardsRow(
                        totalTransactions = transactionVM.transactions.size,
                        totalSpending = transactionVM.totalSpent,
                        currentBudget = budgetLimit,
                        remainingBudget = budgetRemaining
                    )
                }

                // ── INSIGHTS SUMMARY ─────────────────────────────────────────
                item {
                    InsightsSummaryCard(
                        topCategory = topCategory,
                        budgetRemaining = budgetRemaining,
                        monthlyTxnCount = monthlyTxnCount
                    )
                }

                // ── RECENT TRANSACTIONS (with date) ──────────────────────────
                val recent = transactionVM.transactions.take(10)
                if (recent.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Transactions",
                            color = DarkText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(recent) { txn -> TransactionRow(txn) }
                }

                // ── existing: Budget warning banner ──────────────────────────
                budgetVM.budgetWarning?.let { warning ->
                    item { BudgetWarningBanner(warning) }
                }

                // ── existing: Spending overview card ─────────────────────────
                item {
                    SpendingOverviewCard(
                        totalSpent = transactionVM.totalSpent,
                        monthlySpent = transactionVM.monthlySpent
                    )
                }

                // ── existing: Budget progress card ───────────────────────────
                budgetVM.budget?.let { b ->
                    if (b.monthlyLimit > 0) {
                        item {
                            BudgetProgressCard(
                                spent = b.spentAmount,
                                limit = b.monthlyLimit,
                                remaining = b.remainingAmount
                            )
                        }
                    }
                }

                // ── existing: Category breakdown ─────────────────────────────
                if (transactionVM.categorySummary.isNotEmpty()) {
                    item { CategoryBreakdownCard(transactionVM.categorySummary) }
                }

                // ── existing: Smart Insights ──────────────────────────────────
                if (insights.isNotEmpty()) {
                    item { SmartInsightsCard(insights) }
                }

                // ── existing: Category Pie Chart ──────────────────────────────
                if (categoryData.isNotEmpty()) {
                    item { CategoryPieChartCard(categoryData) }
                }

                // ── existing: Monthly Bar Chart ───────────────────────────────
                if (monthlyData.any { it.amount > 0f }) {
                    item { MonthlyBarChartCard(monthlyData) }
                }

                // ── existing: Investments shortcut ────────────────────────────
                item {
                    InvestmentsShortcutCard(
                        totalInvested = investmentVM.totalInvested,
                        onClick = { navController.navigate(Routes.INVESTMENTS) }
                    )
                }

                // ── existing: Export Report ───────────────────────────────────
                if (transactionVM.transactions.isNotEmpty()) {
                    item {
                        ExportReportCard(
                            onExportCsv = {
                                ReportGenerator.shareCsv(context, transactionVM.transactions)
                            },
                            onExportPdf = {
                                ReportGenerator.sharePdf(
                                    context,
                                    transactionVM.transactions,
                                    budgetVM.budget?.monthlyLimit ?: 0.0
                                )
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Dashboard Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A5C))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "FinCortex Dashboard",
                color = DarkAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Welcome back! Here's your financial overview.",
                color = DarkText.copy(alpha = 0.75f),
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Budget Exceeded Alert
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BudgetExceededAlert() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚠", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Budget exceeded!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Summary Cards Row (2 × 2 grid)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCardsRow(
    totalTransactions: Int,
    totalSpending: Double,
    currentBudget: Double,
    remainingBudget: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                label = "Total Transactions",
                value = "$totalTransactions",
                valueColor = DarkAccent,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Total Spending",
                value = "₹${"%.0f".format(totalSpending)}",
                valueColor = Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                label = "Current Budget",
                value = if (currentBudget > 0) "₹${"%.0f".format(currentBudget)}" else "Not set",
                valueColor = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Remaining",
                value = if (currentBudget > 0) "₹${"%.0f".format(remainingBudget)}" else "—",
                valueColor = if (remainingBudget >= 0) Color(0xFF81C784) else Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                color = DarkText.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Insights Summary Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InsightsSummaryCard(
    topCategory: String,
    budgetRemaining: Double,
    monthlyTxnCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Insights",
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            InsightLine(text = "You spent most on $topCategory")
            Spacer(modifier = Modifier.height(8.dp))
            InsightLine(
                text = "Budget remaining: ₹${"%.2f".format(budgetRemaining)}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            InsightLine(text = "Transactions this month: $monthlyTxnCount")
        }
    }
}

@Composable
private fun InsightLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(DarkAccent, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = DarkText.copy(alpha = 0.85f),
            fontSize = 13.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXISTING (unchanged below)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BudgetWarningBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8B0000))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Yellow)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, color = DarkText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SpendingOverviewCard(totalSpent: Double, monthlySpent: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Spending Overview",
                color = DarkText.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpendingStatItem(label = "Total Spent", amount = totalSpent)
                SpendingStatItem(label = "This Month", amount = monthlySpent)
            }
        }
    }
}

@Composable
private fun SpendingStatItem(label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = DarkText.copy(alpha = 0.6f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "₹${"%.2f".format(amount)}",
            color = DarkAccent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BudgetProgressCard(spent: Double, limit: Double, remaining: Double) {
    val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val progressColor = when {
        progress >= 1f -> Color.Red
        progress >= 0.9f -> Color(0xFFFFA500)
        else -> Color.Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Monthly Budget",
                    color = DarkText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${"%.0f".format(limit)}",
                    color = DarkAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = DarkSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ₹${"%.0f".format(spent)}",
                    color = DarkText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Remaining: ₹${"%.0f".format(remaining)}",
                    color = DarkText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(summary: Map<String, Double>) {
    val total = summary.values.sum().coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Category Breakdown",
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            summary.entries
                .sortedByDescending { it.value }
                .forEach { (category, amount) ->
                    CategoryProgressRow(category = category, amount = amount, total = total)
                    Spacer(modifier = Modifier.height(10.dp))
                }
        }
    }
}

@Composable
private fun CategoryProgressRow(category: String, amount: Double, total: Double) {
    val pct = (amount / total).toFloat().coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category, color = DarkText, fontSize = 13.sp)
            Text(
                text = "₹${"%.2f".format(amount)}",
                color = DarkAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = pct,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = DarkAccent,
            trackColor = DarkSecondary
        )
    }
}

@Composable
private fun SmartInsightsCard(insights: List<SmartInsights.Insight>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DarkAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Insights",
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            insights.forEachIndexed { index, insight ->
                InsightRow(insight)
                if (index < insights.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = DarkSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightRow(insight: SmartInsights.Insight) {
    val (icon, tint) = when (insight.type) {
        SmartInsights.InsightType.WARNING -> Icons.Default.Warning to Color(0xFFFF6B6B)
        SmartInsights.InsightType.TIP     -> Icons.Default.Lightbulb to DarkAccent
        SmartInsights.InsightType.INFO    -> Icons.Default.Info to Color(0xFF64B5F6)
    }
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = insight.message, color = DarkText.copy(alpha = 0.9f), fontSize = 13.sp)
    }
}

@Composable
private fun CategoryPieChartCard(data: List<AnalyticsUtils.CategoryData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Category Spending",
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChartHelper.PieChartView(data = data)
        }
    }
}

@Composable
private fun MonthlyBarChartCard(data: List<AnalyticsUtils.MonthlyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Monthly Spending (Last 6 Months)",
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChartHelper.BarChartView(data = data)
        }
    }
}

@Composable
private fun ExportReportCard(onExportCsv: () -> Unit, onExportPdf: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Export Report",
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onExportCsv,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CSV", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSecondary)
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DarkAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF", color = DarkAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentsShortcutCard(totalInvested: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = DarkAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My Investments",
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (totalInvested > 0)
                        "Portfolio: ₹${"%.2f".format(totalInvested)}"
                    else
                        "Track your stocks, MFs, FDs & more",
                    color = DarkText.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = DarkAccent.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TransactionRow(txn: TransactionModel) {
    val isCredit = txn.type == "credit"
    val dateLabel = if (txn.date.isNotBlank()) {
        txn.date
    } else if (txn.createdAt > 0) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(txn.createdAt))
    } else {
        "—"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = txn.type,
                tint = if (isCredit) Color.Green else Color.Red
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = txn.merchant.ifBlank { "Unknown" },
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = txn.category,
                    color = DarkText.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Text(
                    text = dateLabel,
                    color = DarkText.copy(alpha = 0.45f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = "${if (isCredit) "+" else "-"}₹${"%.2f".format(txn.amount)}",
                color = if (isCredit) Color.Green else Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
