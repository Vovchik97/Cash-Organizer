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
    val periodType: String = "MONTHLY"
)

class LimitsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val limitsRepo = LimitsRepository(db.limitsDao())
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()

    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    val limitsUiModels: StateFlow<List<LimitsUiModel>> = combine(
        limitsRepo.getLimitsForMonthFlow(currentMonth),
        categoryDao.getByTypeFlow(TransactionType.EXPENSE.name),
        transactionDao.getAllFlow()
    ) { limits, expenseCategories, allTransactions ->

        val result = mutableListOf<LimitsUiModel>()

        // Обрабатываем ВСЕ категории расходов
        expenseCategories.forEach { category ->
            val categoryLimit = limits.find { it.categoryId == category.id }

            // Если лимита НЕТ - показываем с нулевым потрачено
            if (categoryLimit == null) {
                result.add(
                    LimitsUiModel(
                        limit = null,
                        categoryId = category.id,
                        categoryName = category.name,
                        month = currentMonth,
                        limitAmount = 0.0,
                        spentAmount = 0.0, // НУЛЕВОЕ потрачено для категорий без лимита
                        percent = 0,
                        periodType = "MONTHLY"
                    )
                )
                return@forEach
            }

            // Для категорий С лимитом - считаем потраченную сумму ТОЛЬКО с момента создания лимита
            val periodStart = getPeriodStart(categoryLimit.periodType)
            val limitCreatedAt = categoryLimit.createdAt

            // Берем БОЛЕЕ ПОЗДНЮЮ дату: начало периода ИЛИ момент создания лимита
            val startDate = maxOf(periodStart, limitCreatedAt)

            val spentAmount = allTransactions
                .filter { transaction ->
                    transaction.type == TransactionType.EXPENSE &&
                            transaction.category.equals(category.name, ignoreCase = true) &&
                            transaction.date >= startDate // ТОЛЬКО транзакции ПОСЛЕ создания лимита
                }
                .sumOf { it.amount }

            val percent = if (categoryLimit.limitAmount > 0) {
                (spentAmount / categoryLimit.limitAmount * 100).toInt().coerceAtMost(100)
            } else 0

            result.add(
                LimitsUiModel(
                    limit = categoryLimit,
                    categoryId = category.id,
                    categoryName = category.name,
                    month = currentMonth,
                    limitAmount = categoryLimit.limitAmount,
                    spentAmount = spentAmount,
                    percent = percent,
                    periodType = categoryLimit.periodType
                )
            )
        }

        result.sortedBy { it.categoryName }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
                        periodType = periodType,
                        createdAt = System.currentTimeMillis(), // Запоминаем когда создали лимит
                        updatedAt = System.currentTimeMillis()
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

    suspend fun getExpenseCategories(): List<com.example.cashorganizer.data.model.CategoryEntity> {
        return categoryDao.getAllFlow()
            .map { categories ->
                categories.filter { it.type == TransactionType.EXPENSE }
            }
            .first()
    }
}