package com.example.fincortex.ui.home

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkSecondary
import com.example.fincortex.ui.theme.DarkText
import com.example.fincortex.ui.theme.Routes

// --- Data Classes for Placeholders ---
data class Account(val name: String, val icon: ImageVector, val balance: String, val lastDigits: String)
data class FinancialGoal(val name: String, val icon: ImageVector, val goal: String, val saved: String, val progress: Float)
data class Transaction(val name: String, val date: String, val amount: String, val icon: ImageVector, val isCredit: Boolean)

@Composable
fun HomeScreen(navController: NavController) {
    var spokenText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                spokenText = results?.get(0) ?: ""

                // Analyze spoken text and navigate
                when {
                    spokenText.contains("profile", ignoreCase = true) -> navController.navigate(Routes.PROFILE)
                    spokenText.contains("growth", ignoreCase = true) -> navController.navigate(Routes.GROWTH)
                    spokenText.contains("advisor", ignoreCase = true) -> navController.navigate(Routes.ADVISOR)
                    // Add more commands as needed
                }
            }
        }
    )

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            speechRecognizerLauncher.launch(intent)
        } }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(it)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            // Static Header
            Header("Uttaresh Swami", navController)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Total Balance Card
                item {
                    TotalBalanceCard("1,258,440.50", "+12,450.30 (1.2%)")
                }

                // My Accounts Section
                item {
                    SectionHeader("My Accounts", "See All")
                    MyAccountsSection()
                }

                // Financial Goals Section
                item {
                    SectionHeader("Financial Goals", "Add Goal")
                    FinancialGoalsSection()
                }

                // Spending Summary
                item {
                    SectionHeader("Spending Summary", "This Month")
                    SpendingSummarySection()
                }

                // Recent Transactions
                item {
                    SectionHeader("Recent Transactions", "View All")
                    RecentTransactionsSection()
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, onMicClick: () -> Unit) {
    BottomAppBar(
        containerColor = DarkPrimary,
        contentColor = DarkText
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(icon = Icons.Default.Home, label = "Home", onClick = { navController.navigate(Routes.HOME) })
            BottomNavItem(icon = Icons.Default.TrendingUp, label = "Growth", onClick = { navController.navigate(Routes.GROWTH) })
            FloatingActionButton(
                onClick = onMicClick,
                containerColor = DarkAccent,
                contentColor = DarkPrimary,
                modifier = Modifier.size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Microphone")
            }
            BottomNavItem(icon = Icons.Default.Star, label = "Advisor", onClick = { navController.navigate(Routes.ADVISOR) })
            BottomNavItem(icon = Icons.Default.Person, label = "Profile", onClick = { navController.navigate(Routes.PROFILE) })
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
fun Header(name: String, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp), // Added bottom padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Welcome!", color = DarkText.copy(alpha = 0.7f), fontSize = 16.sp)
            Text(text = name, color = DarkText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = DarkText,
            modifier = Modifier.clickable { navController.navigate(Routes.ACTIVITY_HUB) }
        )
    }
}

@Composable
fun TotalBalanceCard(balance: String, monthlyChange: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Total Balance", color = DarkText.copy(alpha = 0.8f))
                TextButton(onClick = { /* Handle click */ }) {
                    Text(text = "View Report", color = DarkAccent)
                }
            }
            Text(text = "₹$balance", color = DarkText, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = monthlyChange, color = Color.Green, fontSize = 14.sp)
        }
    }
}

@Composable
fun MyAccountsSection() {
    val accounts = listOf(
        Account("Checking", Icons.Default.AccountBalance, "2,76,009.60", "...8021"),
        Account("Savings", Icons.Default.Savings, "8,16,000.00", "...2451")
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        accounts.forEach { account ->
            AccountCard(account, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AccountCard(account: Account, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { /* Handle click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = account.icon, contentDescription = account.name, tint = DarkAccent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = account.name, color = DarkText, fontWeight = FontWeight.Bold)
            Text(text = account.lastDigits, color = DarkText.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "₹${account.balance}", color = DarkText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FinancialGoalsSection() {
    val goals = listOf(
        FinancialGoal("New Car Fund", Icons.Default.DirectionsCar, "15,00,000", "9,00,000", 0.6f),
        FinancialGoal("Vacation to Japan", Icons.Default.Flight, "4,00,000", "3,20,000", 0.8f)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goals.forEach { goal -> GoalCard(goal) }
    }
}

@Composable
fun GoalCard(goal: FinancialGoal) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* Handle click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = goal.icon, 
                contentDescription = goal.name, 
                tint = DarkAccent, 
                modifier = Modifier.background(DarkSecondary, CircleShape).padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = goal.name, color = DarkText, fontWeight = FontWeight.Bold)
                Text(text = "Goal: ₹${goal.goal}", color = DarkText.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = goal.progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = DarkAccent,
                    trackColor = DarkSecondary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "${(goal.progress * 100).toInt()}%", color = DarkAccent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SpendingSummarySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Total Spent", color = DarkText.copy(alpha = 0.7f))
            Text(text = "₹1,00,384", color = DarkText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            DonutChart()
        }
    }
}

@Composable
fun DonutChart() {
    val chartData = listOf(0.4f, 0.2f, 0.3f, 0.1f)
    val colors = listOf(Color.Green, Color.Red, DarkAccent, Color.Gray)
    var startAngle = -90f

    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            chartData.forEachIndexed { index, value ->
                val sweepAngle = value * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 25f, cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
        Text(text = "₹1,00,384", color = DarkText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}


@Composable
fun RecentTransactionsSection() {
    val transactions = listOf(
        Transaction("Whole Foods Market", "Today", "-₹6,840.00", Icons.Default.ShoppingCart, false),
        Transaction("Direct Deposit", "Yesterday", "+₹1,68,000.00", Icons.Default.ArrowDownward, true),
        Transaction("City Transit", "Oct 28", "-₹220.00", Icons.Default.DirectionsCar, false),
        Transaction("Con Edison", "Oct 27", "-₹8,984.00", Icons.Default.ArrowUpward, false)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        transactions.forEach { transaction -> TransactionItem(transaction) }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* Handle click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = transaction.icon, contentDescription = transaction.name, tint = DarkAccent)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.name, color = DarkText, fontWeight = FontWeight.Bold)
                Text(text = transaction.date, color = DarkText.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Text(
                text = transaction.amount,
                color = if (transaction.isCredit) Color.Green else Color.Red,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = { /* Handle Click */ }) {
            Text(text = actionText, color = DarkAccent, fontWeight = FontWeight.SemiBold)
        }
    }
}
