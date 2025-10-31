package com.example.cashorganizer.data.repository

import com.example.cashorganizer.data.dao.TransactionDao
import com.example.cashorganizer.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository (private val dao: TransactionDao) {

    fun getAllFlow(): Flow<List<TransactionEntity>> = dao.getAllFlow()

    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun getTotalIncome(): Double = dao.getTotalIncome() ?: 0.0

    suspend fun getTotalExpense(): Double = dao.getTotalExpense() ?: 0.0
}