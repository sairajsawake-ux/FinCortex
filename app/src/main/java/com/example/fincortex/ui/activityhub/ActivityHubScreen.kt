package com.example.fincortex.ui.activityhub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkText

data class NotificationItemData(val title: String, val description: String, val amount: String?, val time: String, val icon: ImageVector)

@Composable
fun ActivityHubScreen(navController: NavController) {
    val notifications = listOf(
        NotificationItemData("Interest Credit", "Quarterly interest credited to ICICI", "₹1,240.00", "1M AGO", Icons.Default.MonetizationOn),
        NotificationItemData("FinCortex AI", "Reliance Ind. dropped by 2.1%. Review stop-loss levels.", null, "1M AGO", Icons.Default.Warning),
        NotificationItemData("Uber Rides", "debited from HDFC Bank", "₹450.00", "1M AGO", Icons.Default.PhoneAndroid),
        NotificationItemData("Shell Gas Station", "debited from ICICI Bank", "₹2,500.00", "2M AGO", Icons.Default.LocalGasStation),
        NotificationItemData("FinCortex AI", "Bitcoin is up by 5%. Consider your position.", null, "2M AGO", Icons.Default.Warning)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Text(text = "Activity Hub", color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* TODO */ }) {
                Text("CLEAR ALL", color = DarkAccent)
            }
        }
        Text(text = "${notifications.size} RECENT NOTIFICATIONS", color = DarkText.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = notification.icon,
                contentDescription = notification.title,
                tint = DarkAccent,
                modifier = Modifier
                    .background(DarkPrimary.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notification.title, color = DarkText, fontWeight = FontWeight.Bold)
                Text(text = notification.description, color = DarkText.copy(alpha = 0.8f), fontSize = 14.sp)
                if (notification.amount != null) {
                    Text(text = notification.amount, color = DarkAccent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Text(text = notification.time, color = DarkText.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}