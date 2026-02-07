package com.example.fincortex.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fincortex.ui.home.HomeScreen
import com.example.fincortex.ui.login.LoginScreen
import com.example.fincortex.ui.security.SecurityScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SECURITY
    ) {

        composable(Routes.SECURITY) {
            SecurityScreen(
                onVerificationSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SECURITY) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}
