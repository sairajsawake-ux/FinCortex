package com.example.fincortex.ui.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkSecondary
import com.example.fincortex.ui.theme.DarkText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GrowthScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
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
        item {
            MarketNewsCard()
        }
        item {
            TopMoversCard()
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
                 Button(onClick = { /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = DarkSecondary)) { Text("1Y") }
            }
        }
    }
}

@Composable
fun LineChart() {
    val points = listOf(120f, 150f, 130f, 180f, 160f, 190f, 140f, 170f, 200f, 220f)
    val yAxisValues = listOf(100f, 150f, 200f, 250f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) { 
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        val textPaint = Paint().asFrameworkPaint().apply {
            color = DarkText.copy(alpha = 0.6f).toArgb()
            textSize = 12.sp.toPx()
        }

        // Draw grid lines and Y-axis labels
        yAxisValues.forEach { yValue ->
            val y = size.height * (1 - (yValue - 100f) / 150f)
            drawLine(Color.Gray, start = Offset(0f, y), end = Offset(size.width, y), pathEffect = pathEffect, alpha = 0.5f)
            drawIntoCanvas {
                it.nativeCanvas.drawText(yValue.toInt().toString(), -40f, y + 5, textPaint)
            }
        }

        // Draw line graph and data points
        for (i in 0 until points.size - 1) {
            val startX = (size.width / (points.size - 1)) * i
            val startY = size.height * (1 - (points[i] - 100f) / 150f)
            val endX = (size.width / (points.size - 1)) * (i + 1)
            val endY = size.height * (1 - (points[i+1] - 100f) / 150f)
            drawLine(DarkAccent, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 5f)

            // Draw value text above each point
            drawIntoCanvas {
                it.nativeCanvas.drawText(points[i].toInt().toString(), startX, startY - 10, textPaint)
            }
        }
        // Draw the last point's value
        drawIntoCanvas {
             val lastIndex = points.size - 1
             val x = (size.width / lastIndex) * lastIndex
             val y = size.height * (1 - (points[lastIndex] - 100f) / 150f)
            it.nativeCanvas.drawText(points[lastIndex].toInt().toString(), x, y - 10, textPaint)
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
            Spacer(modifier = Modifier.height(16.dp))
            AssetLegend()
        }
    }
}

@Composable
fun PieChart() {
    val assets = mapOf("Stocks" to 60f, "Bitcoin" to 25f, "ETFs" to 15f, "Gold" to 10f, "Real Estate" to 5f)
    val colors = listOf(DarkAccent, Color.Gray, DarkSecondary, Color.LightGray, Color.DarkGray)
    val total = assets.values.sum()
    var startAngle = 0f

    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            assets.entries.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value / total) * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                // Add percentage text
                val angleInRadians = (startAngle + sweepAngle / 2) * (Math.PI / 180)
                val radius = size.width / 3
                val x = (center.x + radius * cos(angleInRadians)).toFloat()
                val y = (center.y + radius * sin(angleInRadians)).toFloat()
                val percentage = (entry.value / total * 100).toInt()

                drawIntoCanvas {
                    val textPaint = Paint().asFrameworkPaint().apply {
                        color = Color.White.toArgb()
                        textSize = 14.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    it.nativeCanvas.drawText("$percentage%", x, y, textPaint)
                }

                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun AssetLegend() {
    val assets = mapOf("Stocks" to 60f, "Bitcoin" to 25f, "ETFs" to 15f, "Gold" to 10f, "Real Estate" to 5f)
    val colors = listOf(DarkAccent, Color.Gray, DarkSecondary, Color.LightGray, Color.DarkGray)

    Column(modifier = Modifier.fillMaxWidth()) {
        assets.keys.forEachIndexed { index, name ->
            LegendItem(name = name, color = colors[index])
        }
    }
}

@Composable
fun LegendItem(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(16.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, color = DarkText, fontSize = 14.sp)
    }
}

@Composable
fun MarketNewsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Market News", color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Stock futures are flat as Wall Street looks to extend its July rally", color = DarkText)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Investors are looking ahead to a busy week of earnings reports and economic data.", color = DarkText.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@Composable
fun TopMoversCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top Movers", color = DarkText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AAPL", color = DarkText)
                Text("195.89", color = DarkText)
                Row {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.Green)
                    Text("1.2%", color = Color.Green)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GOOGL", color = DarkText)
                Text("132.58", color = DarkText)
                Row {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Red)
                    Text("0.8%", color = Color.Red)
                }
            }
        }
    }
}
