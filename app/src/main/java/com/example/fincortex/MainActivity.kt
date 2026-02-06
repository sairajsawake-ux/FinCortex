package com.example.fincortex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fincortex.ui.ExpenseScreen
import com.example.fincortex.ui.SplashScreen
import com.example.fincortex.ui.theme.FinCortexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinCortexTheme {

                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen {
                        showSplash = false
                    }
                } else {
                    ExpenseScreen(
                        viewModel = viewModel(),
                        userId = "test_user_001"
                    )
                }
            }
        }
    }
}
