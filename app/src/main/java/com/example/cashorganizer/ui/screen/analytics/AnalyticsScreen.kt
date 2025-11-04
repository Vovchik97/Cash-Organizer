package com.example.cashorganizer.ui.screen.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.cashorganizer.viewmodel.AnalyticsPeriod
import com.example.cashorganizer.viewmodel.AnalyticsViewModel
import kotlin.math.absoluteValue

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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Аналитика", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
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
                AnalyticsCard("Доход", analytics.totalIncome, Color.Green)
                AnalyticsCard("Расход", analytics.totalExpense, MaterialTheme.colorScheme.error)
                AnalyticsCard("Баланс", analytics.balance, if (analytics.balance >= 0) Color.Blue else MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeriodButton("Месяц", period == AnalyticsPeriod.MONTH) { viewModel.setPeriod(AnalyticsPeriod.MONTH) }
                PeriodButton("Год", period == AnalyticsPeriod.YEAR) { viewModel.setPeriod(AnalyticsPeriod.YEAR) }
                PeriodButton("Всё", period == AnalyticsPeriod.ALL) { viewModel.setPeriod(AnalyticsPeriod.ALL) }
            }

            Spacer(Modifier.height(24.dp))

            if (incomePoints.isNotEmpty() && expensePoints.isNotEmpty()) {
                val lineChartData = LineChartData(
                    linePlotData = LinePlotData(
                        lines = listOf(
                            Line(
                                dataPoints = incomePoints,
                                lineStyle = LineStyle(color = Color.Green),
                                intersectionPoint = IntersectionPoint(radius = 6.dp, color = Color.Green),
                                selectionHighlightPoint = SelectionHighlightPoint()
                            ),
                            Line(
                                dataPoints = expensePoints,
                                lineStyle = LineStyle(color = MaterialTheme.colorScheme.error),
                                intersectionPoint = IntersectionPoint(radius = 6.dp, color = MaterialTheme.colorScheme.error),
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
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LineChart(modifier = Modifier.height(250.dp).fillMaxWidth(), lineChartData = lineChartData)
                }
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
                Text("Распределение расходов", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                PieChart(
                    modifier = Modifier.size(300.dp),
                    pieChartData = pieData,
                    pieChartConfig = pieConfig
                )
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: Double, color: Color) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(
                String.format("%.2f", value),
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
fun PeriodButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
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


/*
package com.example.cashorganizer.ui.screen.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.cashorganizer.viewmodel.AnalyticsPeriod
import com.example.cashorganizer.viewmodel.AnalyticsViewModel
import kotlin.math.absoluteValue

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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Аналитика",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Карточки статистики
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsCard(
                    title = "Доход",
                    value = analytics.totalIncome,
                    icon = Icons.Default.KeyboardArrowUp,
                    color = MaterialTheme.colorScheme.primary
                )
                AnalyticsCard(
                    title = "Расход",
                    value = analytics.totalExpense,
                    icon = Icons.Default.KeyboardArrowDown,
                    color = MaterialTheme.colorScheme.error
                )
                AnalyticsCard(
                    title = "Баланс",
                    value = analytics.balance,
                    icon = Icons.Default.Info,
                    color = if (analytics.balance >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            // Переключатель периода
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Период анализа",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PeriodChip("Месяц", period == AnalyticsPeriod.MONTH) {
                            viewModel.setPeriod(AnalyticsPeriod.MONTH)
                        }
                        PeriodChip("Год", period == AnalyticsPeriod.YEAR) {
                            viewModel.setPeriod(AnalyticsPeriod.YEAR)
                        }
                        PeriodChip("Всё время", period == AnalyticsPeriod.ALL) {
                            viewModel.setPeriod(AnalyticsPeriod.ALL)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // График доходов/расходов
            if (incomePoints.isNotEmpty() && expensePoints.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Динамика доходов и расходов",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val lineChartData = LineChartData(
                            linePlotData = LinePlotData(
                                lines = listOf(
                                    Line(
                                        dataPoints = incomePoints,
                                        lineStyle = LineStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                        ),
                                        intersectionPoint = IntersectionPoint(
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        selectionHighlightPoint = SelectionHighlightPoint(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    Line(
                                        dataPoints = expensePoints,
                                        lineStyle = LineStyle(
                                            color = MaterialTheme.colorScheme.error,
                                        ),
                                        intersectionPoint = IntersectionPoint(
                                            color = MaterialTheme.colorScheme.error
                                        ),
                                        selectionHighlightPoint = SelectionHighlightPoint(
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                )
                            ),
                            xAxisData = AxisData.Builder()
                                .axisStepSize(60.dp)
                                .steps(xLabels.size - 1)
                                .labelData { i -> xLabels.getOrNull(i) ?: "" }
                                .axisLabelAngle(45f)
                                .build(),
                            yAxisData = AxisData.Builder()
                                .steps(5)
                                .build()
                        )

                        LineChart(
                            modifier = Modifier
                                .height(250.dp)
                                .fillMaxWidth(),
                            lineChartData = lineChartData
                        )

                        // Легенда
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LegendItem("Доход", MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            LegendItem("Расход", MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Круговая диаграмма расходов
            if (analytics.categoryStats.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Распределение расходов по категориям",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            isEllipsizeEnabled = true,
                            chartPadding = 20
                        )

                        PieChart(
                            modifier = Modifier.size(280.dp),
                            pieChartData = pieData,
                            pieChartConfig = pieConfig
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: Double, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                String.format("%.2f", value) + " ₽",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

@Composable
fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .height(36.dp)
    )
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun categoryColor(category: String): Color {
    val hash = category.hashCode().absoluteValue
    val hue = (hash % 359 + 1).toFloat()
    val hsvColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.95f))
    return Color(hsvColor)
}*/
