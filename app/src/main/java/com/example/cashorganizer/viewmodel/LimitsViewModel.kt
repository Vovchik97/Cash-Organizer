package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.LimitsEntity
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.data.repository.LimitsRepository
import com.example.cashorganizer.utils.DateUtils.getStartOfDay
import com.example.cashorganizer.utils.DateUtils.getStartOfMonth
import com.example.cashorganizer.utils.DateUtils.getStartOfWeek
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class LimitsUiModel(
    val limit: LimitsEntity?,
    val categoryId: Long?,
    val categoryName: String,
    val month: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val percent: Int,
    val periodType: String = "MONTHLY" // Добавляем тип периода
)

class LimitsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val limitsRepo = LimitsRepository(db.limitsDao()) // Меняем на limitsDao
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()

    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    val limitsUiModels: StateFlow<List<LimitsUiModel>> = combine(
        limitsRepo.getLimitsForMonthFlow(currentMonth), // Меняем название метода
        categoryDao.getByTypeFlow(TransactionType.EXPENSE.name),
        transactionDao.getAllFlow()
    ) { limits, expenseCategories, allTransactions ->

        val expensesByCategory = mutableMapOf<Long, Double>()

        allTransactions.forEach { tx ->
            if (tx.type == TransactionType.EXPENSE && tx.category.isNotEmpty()) {
                val category = expenseCategories.find {
                    it.name.equals(tx.category, ignoreCase = true)
                }
                if (category != null) {
                    // Определяем начало периода для каждого лимита
                    val limit = limits.find { it.categoryId == category.id }
                    val periodStart = getPeriodStart(limit?.periodType ?: "MONTHLY")

                    // Учитываем только транзакции за текущий период
                    if (tx.date >= periodStart) {
                        expensesByCategory[category.id] =
                            expensesByCategory.getOrDefault(category.id, 0.0) + tx.amount
                    }
                }
            }
        }

        val result = mutableListOf<LimitsUiModel>()

        // Категории с лимитом
        limits.forEach { limit ->
            val cat = expenseCategories.find { it.id == limit.categoryId }
            if (cat != null) {
                val spent = expensesByCategory[limit.categoryId] ?: 0.0
                val percent = if (limit.limitAmount > 0) {
                    (spent / limit.limitAmount * 100).toInt().coerceAtMost(100)
                } else 0

                result.add(
                    LimitsUiModel(
                        limit = limit,
                        categoryId = limit.categoryId,
                        categoryName = cat.name,
                        month = limit.month,
                        limitAmount = limit.limitAmount,
                        spentAmount = spent,
                        percent = percent,
                        periodType = limit.periodType
                    )
                )
            }
        }

        // Категории без лимита
        expenseCategories.forEach { cat ->
            if (!limits.any { it.categoryId == cat.id }) {
                // Для категорий без лимита считаем траты за текущий месяц
                val spent = expensesByCategory[cat.id] ?: 0.0
                result.add(
                    LimitsUiModel(
                        limit = null,
                        categoryId = cat.id,
                        categoryName = cat.name,
                        month = currentMonth,
                        limitAmount = 0.0,
                        spentAmount = spent,
                        percent = 0,
                        periodType = "MONTHLY"
                    )
                )
            }
        }

        result.sortedBy { it.categoryName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun getPeriodStart(periodType: String): Long {
        return when (periodType) {
            "DAILY" -> getStartOfDay()
            "WEEKLY" -> getStartOfWeek()
            "MONTHLY" -> getStartOfMonth()
            else -> getStartOfMonth()
        }
    }

    fun setLimitForCategory(categoryId: Long, month: String, limit: Double, periodType: String = "MONTHLY") {
        viewModelScope.launch {
            // Проверяем, что категория расходная перед установкой лимита
            val category = categoryDao.getById(categoryId)
            if (category?.type != TransactionType.EXPENSE) {
                println("⚠️ Предупреждение: попытка установить лимит для нерасходной категории")
                return@launch
            }

            if (limit > 0) {
                val existing = limitsRepo.getByCategoryAndMonth(categoryId, month)
                if (existing != null) {
                    limitsRepo.update(
                        existing.copy(
                            limitAmount = limit,
                            periodType = periodType,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    val newLimit = LimitsEntity(
                        categoryId = categoryId,
                        month = month,
                        limitAmount = limit,
                        periodType = periodType
                    )
                    limitsRepo.insert(newLimit)
                }
            } else {
                limitsRepo.getByCategoryAndMonth(categoryId, month)?.let { limitsRepo.delete(it) }
            }
        }
    }

    fun deleteLimit(limit: LimitsEntity) {
        viewModelScope.launch { limitsRepo.delete(limit) }
    }

    // Метод для получения только расходных категорий
    suspend fun getExpenseCategories() = categoryDao.getAllFlow()
        .map { categories -> categories.filter { it.type == TransactionType.EXPENSE } }
        .first()
}