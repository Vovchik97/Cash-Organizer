package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.BudgetEntity
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BudgetUiModel(
    val budget: BudgetEntity?,
    val categoryId: Long?,
    val categoryName: String,
    val month: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val percent: Int
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val budgetRepo = BudgetRepository(db.budgetDao())
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()

    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    val budgetUiModels: StateFlow<List<BudgetUiModel>> = combine(
        budgetRepo.getBudgetsForMonthFlow(currentMonth),
        categoryDao.getAllFlow(),
        transactionDao.getAllFlow()
    ) { budgets, categories, allTransactions ->
        println("🔥 DEBUG: ${allTransactions.size} транзакций найдено")

        val expensesByCategory = mutableMapOf<Long, Double>()

        // ФИКС: учитываем категорию из поля category, а не categoryId
        allTransactions.forEach { tx ->
            val txMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(tx.date))
            println("🔥 Транзакция: ${tx.amount} руб, категория='${tx.category}', месяц=$txMonth, тип=${tx.type}")

            if (tx.type == TransactionType.EXPENSE &&
                tx.category.isNotEmpty() &&
                txMonth == currentMonth
            ) {

                // Ищем категорию по имени, а не по ID
                val category = categories.find { it.name.equals(tx.category, ignoreCase = true) }
                if (category != null) {
                    expensesByCategory[category.id] =
                        expensesByCategory.getOrDefault(category.id, 0.0) + tx.amount
                    println("🔥 Добавлено к категории ${category.name} (id=${category.id}): ${tx.amount} руб")
                } else {
                    println("🔥 Категория '${tx.category}' не найдена в базе категорий")
                }
            }
        }

        println("🔥 Итоги по категориям: $expensesByCategory")

        val result = mutableListOf<BudgetUiModel>()

        // Категории с бюджетом
        budgets.forEach { b ->
            val cat = categories.find { it.id == b.categoryId }
            if (cat != null) {
                val spent = expensesByCategory[b.categoryId] ?: 0.0
                val percent = if (b.limitAmount > 0) {
                    (spent / b.limitAmount * 100).toInt().coerceAtMost(100)
                } else 0

                println("🔥 Бюджет для ${cat.name}: лимит=${b.limitAmount}, потрачено=$spent")

                result.add(
                    BudgetUiModel(
                        budget = b,
                        categoryId = b.categoryId,
                        categoryName = cat.name,
                        month = b.month,
                        limitAmount = b.limitAmount,
                        spentAmount = spent,
                        percent = percent
                    )
                )
            }
        }

        // Категории без бюджета (только текущий месяц)
        categories.forEach { cat ->
            if (!budgets.any { it.categoryId == cat.id }) {
                val spent = expensesByCategory[cat.id] ?: 0.0
                result.add(
                    BudgetUiModel(
                        budget = null,
                        categoryId = cat.id,
                        categoryName = cat.name,
                        month = currentMonth,
                        limitAmount = 0.0,
                        spentAmount = spent,
                        percent = 0
                    )
                )
            }
        }

        result.sortedBy { it.categoryName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Остальные методы без изменений
    fun setBudgetForCategory(categoryId: Long, month: String, limit: Double) {
        viewModelScope.launch {
            if (limit > 0) {
                val existing = budgetRepo.getByCategoryAndMonth(categoryId, month)
                if (existing != null) {
                    budgetRepo.update(
                        existing.copy(
                            limitAmount = limit,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    val b =
                        BudgetEntity(categoryId = categoryId, month = month, limitAmount = limit)
                    budgetRepo.insert(b)
                }
            } else {
                budgetRepo.getByCategoryAndMonth(categoryId, month)?.let { budgetRepo.delete(it) }
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch { budgetRepo.delete(budget) }
    }
}