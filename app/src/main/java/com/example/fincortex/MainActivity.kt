package com.example.fincortex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fincortex.ui.theme.FinCortexTheme
import com.example.fincortex.ui.theme.NavGraph



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinCortexTheme {
                NavGraph()
            }
        }

    }
}
