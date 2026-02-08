package com.example.fincortex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fincortex.ui.activityhub.ActivityHubScreen
import com.example.fincortex.ui.advisor.AdvisorScreen
import com.example.fincortex.ui.growth.GrowthScreen
import com.example.fincortex.ui.home.HomeScreen
import com.example.fincortex.ui.login.LoginScreen
import com.example.fincortex.ui.profile.ProfileScreen
import com.example.fincortex.ui.security.SecurityScreen
import com.example.fincortex.ui.settings.SettingsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.SECURITY) {
            val auth = FirebaseAuth.getInstance()
            SecurityScreen(
                onVerificationSuccess = {
                    val route = if (auth.currentUser != null) Routes.HOME else Routes.LOGIN
                    navController.navigate(route) {
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
            HomeScreen(navController = navController)
        }

        composable(Routes.GROWTH) {
            GrowthScreen(navController = navController)
        }

        composable(Routes.ADVISOR) {
            AdvisorScreen(navController = navController)
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ACTIVITY_HUB) {
            ActivityHubScreen(navController = navController)
        }

        composable(Routes.APP_SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
