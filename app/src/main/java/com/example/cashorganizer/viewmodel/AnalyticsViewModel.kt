package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AnalyticsPeriod { MONTH, YEAR, ALL }

data class AnalyticsData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val stats: Map<String, Pair<Double, Double>> = emptyMap(), // единое поле для всех периодов
    val categoryStats: Map<String, Double> = emptyMap()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).transactionDao()

    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData = _analyticsData.asStateFlow()

    private val _period = MutableStateFlow(AnalyticsPeriod.MONTH)
    val period = _period.asStateFlow()

    fun setPeriod(period: AnalyticsPeriod) {
        _period.value = period
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            val transactions = dao.getAllOnce()
            val now = Calendar.getInstance()
            val (filtered, formatter) = when (_period.value) {
                AnalyticsPeriod.MONTH -> {
                    val filtered = transactions.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                    }
                    val formatter = SimpleDateFormat("dd", Locale.getDefault())
                    filtered to formatter
                }
                AnalyticsPeriod.YEAR -> {
                    val filtered = transactions.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    }
                    val formatter = SimpleDateFormat("MMM", Locale.getDefault())
                    filtered to formatter
                }
                AnalyticsPeriod.ALL -> {
                    val filtered = transactions
                    val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
                    filtered to formatter
                }
            }

            val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = income - expense

            val stats = filtered.groupBy { formatter.format(Date(it.date)) }
                .mapValues { entry ->
                    val inc = entry.value.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val exp = entry.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    Pair(inc, exp)
                }
                .toSortedMap(compareBy(formatter)) // сортируем по дате

            val categoryStats = filtered
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }

            _analyticsData.value = AnalyticsData(
                totalIncome = income,
                totalExpense = expense,
                balance = balance,
                stats = stats,
                categoryStats = categoryStats
            )
        }
    }

    // Вспомогательная функция для сортировки по дате
    private fun compareBy(formatter: SimpleDateFormat): Comparator<String> = compareBy { key ->
        try {
            when (_period.value) {
                AnalyticsPeriod.MONTH -> {
                    // Дополняем до "dd.MM.yyyy" для корректного парсинга
                    val now = Calendar.getInstance()
                    val year = now.get(Calendar.YEAR)
                    val month = now.get(Calendar.MONTH) + 1
                    val fullDate = "$key.$month.$year"
                    SimpleDateFormat("dd.M.yyyy", Locale.getDefault()).parse(fullDate)
                }
                AnalyticsPeriod.YEAR -> {
                    // "MMM" -> "Jan" и т.д. — парсим как 1-е число месяца
                    val now = Calendar.getInstance()
                    val year = now.get(Calendar.YEAR)
                    val fullDate = "$key 1, $year"
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).parse(fullDate)
                }
                AnalyticsPeriod.ALL -> {
                    SimpleDateFormat("yyyy", Locale.getDefault()).parse(key)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}