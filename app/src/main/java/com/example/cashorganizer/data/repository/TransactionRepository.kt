package com.example.cashorganizer.data.repository

import com.example.cashorganizer.data.dao.TransactionDao
import com.example.cashorganizer.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository (private val dao: TransactionDao) {

    fun getAllFlow(): Flow<List<TransactionEntity>> = dao.getAllFlow()

    suspend fun getAllOnce(): List<TransactionEntity> = dao.getAllOnce()

    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun getTotalIncome(): Double = dao.getTotalIncome() ?: 0.0

    suspend fun getTotalExpense(): Double = dao.getTotalExpense() ?: 0.0

    fun getByDateRangeFlow(start: Long, end: Long): Flow<List<TransactionEntity>> = dao.getByDateRangeFlow(start, end)

    suspend fun getByDateRangeOnce(start: Long, end: Long): List<TransactionEntity> = dao.getByDateRangeOnce(start, end)
}