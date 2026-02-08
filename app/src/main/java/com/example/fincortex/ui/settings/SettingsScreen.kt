package com.example.fincortex.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkText
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(navController: NavController, onLogout: () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Text(text = "Settings", color = DarkText, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Notification Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = DarkAccent)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Notification", color = DarkText, modifier = Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { notificationsEnabled = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (notificationsEnabled) DarkAccent else DarkPrimary,
                            contentColor = if (notificationsEnabled) DarkPrimary else DarkText
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("ON", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { notificationsEnabled = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!notificationsEnabled) DarkAccent else DarkPrimary,
                            contentColor = if (!notificationsEnabled) DarkPrimary else DarkText
                        ),
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("OFF", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    FirebaseAuth.getInstance().signOut()
                    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        remove("auth_token")
                        apply()
                    }
                    onLogout()
                },
            colors = CardDefaults.cardColors(containerColor = DarkPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = DarkAccent)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Logout", color = DarkText, modifier = Modifier.weight(1f))
            }
        }
    }
}
