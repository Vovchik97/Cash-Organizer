package com.example.cashorganizer.data.repository

import com.example.cashorganizer.data.dao.BudgetDao
import com.example.cashorganizer.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {
    fun getAllFlow(): Flow<List<BudgetEntity>> = dao.getAllFlow()
    suspend fun insert(budget: BudgetEntity): Long = dao.insert(budget)
    suspend fun update(budget: BudgetEntity) = dao.update(budget)
    suspend fun delete(budget: BudgetEntity) = dao.delete(budget)
    suspend fun getByCategoryAndMonth(categoryId: Long, month: String): BudgetEntity? =
        dao.getByCategoryAndMonth(categoryId, month)
    fun getBudgetsForMonthFlow(month: String): Flow<List<BudgetEntity>> =
        dao.getBudgetsForMonthFlow(month)
}