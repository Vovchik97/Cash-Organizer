package com.example.cashorganizer.data.repository

import com.example.cashorganizer.data.dao.LimitsDao
import com.example.cashorganizer.data.model.LimitsEntity
import kotlinx.coroutines.flow.Flow

class LimitsRepository(private val dao: LimitsDao) {
    fun getAllFlow(): Flow<List<LimitsEntity>> = dao.getAllFlow()
    suspend fun insert(budget: LimitsEntity): Long = dao.insert(budget)
    suspend fun update(budget: LimitsEntity) = dao.update(budget)
    suspend fun delete(budget: LimitsEntity) = dao.delete(budget)
    suspend fun getByCategoryAndMonth(categoryId: Long, month: String): LimitsEntity? =
        dao.getByCategoryAndMonth(categoryId, month)
    fun getLimitsForMonthFlow(month: String): Flow<List<LimitsEntity>> =
        dao.getLimitsForMonthFlow(month)
}