package com.example.fincortex

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.example.fincortex.ui.navigation.NavGraph
import com.example.fincortex.ui.navigation.Routes
import com.example.fincortex.ui.splash.SplashScreen
import com.example.fincortex.ui.theme.FinCortexTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        // Always start with security check
        val startDestination = Routes.SECURITY

        if (currentUser != null) {
            // Fetch and store token if user is logged in
            currentUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val token = tokenTask.result?.token
                    if (token != null) {
                        val sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("auth_token", token)
                            apply()
                        }
                    }
                }
            }
        }

        setContent {
            FinCortexTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen { showSplash = false }
                } else {
                    NavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
