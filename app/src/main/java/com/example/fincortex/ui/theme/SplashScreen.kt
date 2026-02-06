package com.example.fincortex.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {
    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate("expense") {
            popUpTo("splash") { inclusive = true }
        }
    }
}
