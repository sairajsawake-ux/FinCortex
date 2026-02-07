package com.example.fincortex.ui.advisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkText

@Composable
fun AdvisorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Advisor Page", color = DarkText)
    }
}
