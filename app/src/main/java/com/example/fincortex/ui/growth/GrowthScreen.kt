package com.example.fincortex.ui.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkSecondary
import com.example.fincortex.ui.theme.DarkText

@Composable
fun GrowthScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
                }
                Text(text = "Growth", color = DarkText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            PerformanceChartCard()
        }
        item {
            AssetAllocationCard()
        }
    }
}

@Composable
fun PerformanceChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Performance", color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LineChart()
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)) { Text("1M") }
                Button(onClick = { /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = DarkSecondary)) { Text("3M") }
                Button(onClick = { /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = DarkSecondary)) { Text("6M") }
            }
        }
    }
}

@Composable
fun LineChart() {
    val points = listOf(0.2f, 0.5f, 0.3f, 0.8f, 0.6f, 0.9f, 0.4f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) { 
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        // Draw grid lines
        for (i in 0..4) {
            val y = size.height * (i / 4f)
            drawLine(Color.Gray, start = Offset(0f, y), end = Offset(size.width, y), pathEffect = pathEffect, alpha = 0.5f)
        }
        // Draw line graph
        for (i in 0 until points.size - 1) {
            val startX = (size.width / (points.size - 1)) * i
            val startY = size.height * (1 - points[i])
            val endX = (size.width / (points.size - 1)) * (i + 1)
            val endY = size.height * (1 - points[i+1])
            drawLine(DarkAccent, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 5f)
        }
    }
}

@Composable
fun AssetAllocationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Asset Allocation", color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            PieChart()
        }
    }
}

@Composable
fun PieChart() {
    val assets = mapOf("Stocks" to 60f, "Bitcoin" to 25f, "ETFs" to 15f, "Gold" to 10f, "Real Estate" to 5f)
    val colors = listOf(DarkAccent, Color.Gray, DarkSecondary, Color.LightGray, Color.DarkGray)
    var startAngle = 0f
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            assets.values.forEachIndexed { index, value ->
                val sweepAngle = (value / assets.values.sum()) * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }
    }
    // Add legend here if desired
}
