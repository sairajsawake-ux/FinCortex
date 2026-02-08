package com.example.fincortex.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.fincortex.ui.activityhub.ActivityHubScreen
import com.example.fincortex.ui.advisor.AdvisorScreen
import com.example.fincortex.ui.growth.GrowthScreen
import com.example.fincortex.ui.home.HomeScreen
import com.example.fincortex.ui.login.LoginScreen
import com.example.fincortex.ui.profile.ProfileScreen
import com.example.fincortex.ui.security.SecurityScreen
import com.example.fincortex.ui.settings.SettingsScreen

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
            HomeScreen(navController = navController)
        }

        composable(Routes.GROWTH) {
            GrowthScreen(navController = navController)
        }

        composable(Routes.ADVISOR) {
            AdvisorScreen()
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

        composable(
            route = Routes.ACTIVITY_HUB,
            deepLinks = listOf(
                navDeepLink { uriPattern = "fincortex://activity_hub" }
            )
        ) {
            ActivityHubScreen(navController = navController)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
