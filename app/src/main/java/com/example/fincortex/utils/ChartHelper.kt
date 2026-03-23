package com.example.fincortex.utils

import android.graphics.Color as AColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter

/**
 * Compose wrappers for MPAndroidChart views, styled to match the app's dark theme.
 *
 * App palette:
 *   Background / Card: #1F2E3D
 *   Secondary bg:      #2C3E50
 *   Gold accent:       #D4AF37
 *   Text:              #FFFFFF
 */
object ChartHelper {

    /** Slice colours for the pie chart — gold first, then complementary tones. */
    private val SLICE_COLORS = listOf(
        AColor.parseColor("#D4AF37"), // Gold
        AColor.parseColor("#4FC3F7"), // Sky blue
        AColor.parseColor("#81C784"), // Soft green
        AColor.parseColor("#FF8A65"), // Soft orange
        AColor.parseColor("#CE93D8"), // Lavender
        AColor.parseColor("#4DB6AC"), // Teal
        AColor.parseColor("#F48FB1")  // Pink
    )

    private const val CARD_BG = "#1F2E3D"
    private const val SECONDARY_BG = "#2C3E50"

    // -------------------------------------------------------------------------
    // Pie Chart
    // -------------------------------------------------------------------------

    /**
     * Category-spending pie (donut) chart.
     * Displays category names in the legend; percentage values on slices.
     */
    @Composable
    fun PieChartView(
        data: List<AnalyticsUtils.CategoryData>,
        modifier: Modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        AndroidView(
            factory = { context ->
                PieChart(context).apply {
                    setBackgroundColor(AColor.parseColor(CARD_BG))
                    description.isEnabled = false

                    // Donut style
                    isDrawHoleEnabled = true
                    holeRadius = 42f
                    transparentCircleRadius = 47f
                    setHoleColor(AColor.parseColor(CARD_BG))
                    setTransparentCircleColor(AColor.parseColor(CARD_BG))
                    setTransparentCircleAlpha(80)

                    // Centre label
                    setDrawCenterText(true)
                    centerText = "Spending"
                    setCenterTextColor(AColor.WHITE)
                    setCenterTextSize(14f)

                    // Labels on slices — off; we use the legend instead
                    setDrawEntryLabels(false)

                    // Legend
                    legend.isEnabled = true
                    legend.textColor = AColor.WHITE
                    legend.textSize = 11f
                    legend.isWordWrapEnabled = true

                    setNoDataText("No spending data yet")
                    setNoDataTextColor(AColor.WHITE)
                    setUsePercentValues(true)
                    animateY(800)
                }
            },
            update = { chart ->
                if (data.isEmpty()) return@AndroidView

                val entries = data.map { PieEntry(it.amount, it.category) }
                val dataSet = PieDataSet(entries, "").apply {
                    colors = SLICE_COLORS
                    valueTextColor = AColor.WHITE
                    valueTextSize = 10f
                    sliceSpace = 2f
                    valueFormatter = PercentFormatter(chart)
                }
                chart.data = PieData(dataSet)
                chart.invalidate()
            },
            modifier = modifier
        )
    }

    // -------------------------------------------------------------------------
    // Bar Chart
    // -------------------------------------------------------------------------

    /**
     * Monthly spending bar chart showing the last N months.
     * Gold bars on a dark background, ₹ values above each bar.
     */
    @Composable
    fun BarChartView(
        data: List<AnalyticsUtils.MonthlyData>,
        modifier: Modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    setBackgroundColor(AColor.parseColor(CARD_BG))
                    description.isEnabled = false
                    setPinchZoom(false)
                    setDrawGridBackground(false)
                    setDrawBarShadow(false)
                    setDrawValueAboveBar(true)
                    isHighlightPerTapEnabled = false

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        textColor = AColor.WHITE
                        textSize = 11f
                        setDrawGridLines(false)
                        setDrawAxisLine(false)
                        granularity = 1f
                    }

                    axisLeft.apply {
                        textColor = AColor.WHITE
                        textSize = 10f
                        gridColor = AColor.parseColor(SECONDARY_BG)
                        axisLineColor = AColor.parseColor(SECONDARY_BG)
                    }

                    axisRight.isEnabled = false

                    legend.isEnabled = false

                    setNoDataText("No monthly data yet")
                    setNoDataTextColor(AColor.WHITE)
                }
            },
            update = { chart ->
                if (data.isEmpty()) return@AndroidView

                val entries = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.amount) }
                val labels = data.map { it.label }

                val dataSet = BarDataSet(entries, "Monthly Spending").apply {
                    color = AColor.parseColor("#D4AF37")
                    valueTextColor = AColor.WHITE
                    valueTextSize = 9f
                }

                chart.data = BarData(dataSet).apply { barWidth = 0.55f }
                chart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    labelCount = labels.size
                }
                chart.setFitBars(true)
                chart.animateY(700)
                chart.invalidate()
            },
            modifier = modifier
        )
    }
}
