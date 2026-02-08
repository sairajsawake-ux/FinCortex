package com.example.fincortex.ui.advisor

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkText

data class Message(val text: String, val isUser: Boolean)

@Composable
fun AdvisorScreen(navController: NavController) {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateOf(listOf(Message("Hello! I'm your virtual financial advisor. How can I assist you with your finances today?", false))) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Financial Advisor", color = DarkText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(messages.value) { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text = msg.text,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (msg.isUser) DarkAccent else DarkPrimary)
                            .padding(12.dp),
                        color = DarkText
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your question...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkPrimary,
                    unfocusedContainerColor = DarkPrimary,
                    disabledContainerColor = DarkPrimary,
                    cursorColor = DarkAccent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (message.isNotBlank()) {
                    messages.value = messages.value + Message(message, true)
                    messages.value = messages.value + Message(getChatbotResponse(message), false)
                    message = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = DarkAccent)
            }
        }
    }
}

fun getChatbotResponse(message: String): String {
    val lowerCaseMessage = message.lowercase()
    return when {
        // Greetings
        "hello" in lowerCaseMessage || "hi" in lowerCaseMessage -> "Hello! I'm your virtual financial advisor. How can I assist you with your finances today?"

        // Account Balance
        "balance" in lowerCaseMessage || "how much money" in lowerCaseMessage -> "You can find your detailed account balance on the Home screen. It shows your total balance and balances for individual accounts like Checking and Savings."

        // Investments & Growth
        "investment" in lowerCaseMessage || "growth" in lowerCaseMessage || "portfolio" in lowerCaseMessage -> "The Growth screen provides a comprehensive overview of your investment performance, including your asset allocation and market trends. Would you like me to show you the Growth screen?"
        "stocks" in lowerCaseMessage -> "On the Growth screen, you can see how your stocks are performing. We also have a 'Top Movers' section to show you the day's biggest winners and losers."
        "bitcoin" in lowerCaseMessage || "crypto" in lowerCaseMessage -> "Your cryptocurrency holdings are part of your asset allocation on the Growth screen. We can track its performance for you."

        // Financial Goals
        "goal" in lowerCaseMessage || "saving for" in lowerCaseMessage -> "Setting and tracking financial goals is a great way to stay motivated. You can manage your goals on the Home screen. What are you saving for?"
        "new car" in lowerCaseMessage -> "A new car is an exciting goal! You can create a 'New Car Fund' in the Financial Goals section to track your progress."

        // Transactions
        "transaction" in lowerCaseMessage || "spending" in lowerCaseMessage -> "Your recent transactions are available on the Home screen. For a more detailed history and spending summary, check out the Activity Hub."

        // Budgeting & Savings
        "budget" in lowerCaseMessage || "budgeting" in lowerCaseMessage -> "A good budget is key to financial health. Try to categorize your spending to see where your money is going. The Spending Summary on the home page can help with that."
        "save money" in lowerCaseMessage || "savings tips" in lowerCaseMessage -> "A great way to save is to set a portion of your income aside automatically. Even small amounts add up over time! Also, consider creating a specific savings goal."

        // Profile & Security
        "profile" in lowerCaseMessage -> "You can view and manage your personal information in the Profile screen."
        "password" in lowerCaseMessage || "secure" in lowerCaseMessage -> "Your security is our top priority. You can manage your password and other security settings in the Security section."
        
        // General Advice
        "retire" in lowerCaseMessage || "retirement" in lowerCaseMessage -> "Planning for retirement is crucial. A good strategy often involves a mix of investments like stocks and ETFs. It's never too early to start saving for retirement."
        "credit score" in lowerCaseMessage -> "Your credit score is important for future loans and financial opportunities. To improve it, make sure to pay your bills on time and keep your credit card balances low."


        // Fallback
        "help" in lowerCaseMessage -> "I can assist with questions about your account balance, investments, financial goals, transactions, and provide general financial advice. What's on your mind?"
        else -> "That's a great question. While I'm still learning, I can help with topics like account balances, investment tracking, and savings tips. How about we start there?"
    }
}
