package com.example.cashorganizer.ui.screen.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import com.example.cashorganizer.viewmodel.AnalyticsPeriod
import com.example.cashorganizer.viewmodel.AnalyticsViewModel
import co.yml.charts.ui.piechart.models.PieChartData
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val analytics by viewModel.analyticsData.collectAsState()
    val period by viewModel.period.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    val xLabels = analytics.stats.keys.toList()

    val incomePoints = analytics.stats.entries.mapIndexed { index, entry ->
        Point(index.toFloat(), entry.value.first.toFloat())
    }
    val expensePoints = analytics.stats.entries.mapIndexed { index, entry ->
        Point(index.toFloat(), entry.value.second.toFloat())
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Аналитика") }) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalyticsCard("Доход", analytics.totalIncome)
                AnalyticsCard("Расход", analytics.totalExpense)
                AnalyticsCard("Баланс", analytics.balance)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeriodButton("Месяц", period == AnalyticsPeriod.MONTH) { viewModel.setPeriod(
                    AnalyticsPeriod.MONTH)}
                PeriodButton("Год", period == AnalyticsPeriod.YEAR) { viewModel.setPeriod(
                    AnalyticsPeriod.YEAR)}
                PeriodButton("Всё", period == AnalyticsPeriod.ALL) { viewModel.setPeriod(
                    AnalyticsPeriod.ALL)}
            }

            Spacer(Modifier.height(16.dp))

            if (incomePoints.isNotEmpty() && expensePoints.isNotEmpty()) {
                val lineChartData = LineChartData(
                    linePlotData = LinePlotData(
                        lines = listOf(
                            Line(
                                dataPoints = incomePoints,
                                lineStyle = LineStyle(color = MaterialTheme.colorScheme.primary),
                                intersectionPoint = IntersectionPoint(),
                                selectionHighlightPoint = SelectionHighlightPoint()
                            ),
                            Line(
                                dataPoints = expensePoints,
                                lineStyle = LineStyle(color = MaterialTheme.colorScheme.error),
                                intersectionPoint = IntersectionPoint(),
                                selectionHighlightPoint = SelectionHighlightPoint()
                            )
                        )
                    ),
                    xAxisData = AxisData.Builder()
                        .axisStepSize(60.dp)
                        .steps(xLabels.size - 1)
                        .labelData { i -> xLabels.getOrNull(i) ?: "" }
                        .build()
                )
                LineChart(modifier = Modifier.height(250.dp).fillMaxWidth(), lineChartData)
            }

            Spacer(Modifier.height(32.dp))

            if (analytics.categoryStats.isNotEmpty()) {
                val slices = analytics.categoryStats.map { entry ->
                    PieChartData.Slice(
                        label = entry.key,
                        value = entry.value.toFloat(),
                        color = categoryColor(entry.key)
                    )
                }

                val pieData = PieChartData(slices, plotType = PlotType.Pie)

                val pieConfig = PieChartConfig(
                    isAnimationEnable = true,
                    sliceLabelTextColor = MaterialTheme.colorScheme.onSurface,
                    backgroundColor = MaterialTheme.colorScheme.background
                )

                Text(
                    "Распределение расходов по категориям",
                    style = MaterialTheme.typography.titleMedium
                )
                PieChart(
                    modifier = Modifier.size(300.dp),
                    pieChartData = pieData,
                    pieChartConfig = pieConfig)
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: Double) {
    Card(modifier = Modifier
        .width(110.dp)
        .padding(4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(String.format("%.2f", value), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun PeriodButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(label)
    }
}

fun categoryColor(category: String): Color {
    val hash = category.hashCode().absoluteValue
    val hue = (hash % 359 + 1).toFloat()
    val hsvColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.95f))
    return Color(hsvColor)
}