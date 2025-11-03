package com.example.cashorganizer.data.repository

import com.example.cashorganizer.data.dao.GoalDao
import com.example.cashorganizer.data.model.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val dao: GoalDao) {
    fun getAllFlow(): Flow<List<GoalEntity>> = dao.getAllFlow()
    suspend fun insert(goal: GoalEntity): Long = dao.insert(goal)
    suspend fun update(goal: GoalEntity) = dao.update(goal)
    suspend fun delete(goal: GoalEntity) = dao.delete(goal)
    suspend fun getById(id: Long): GoalEntity? = dao.getById(id)
}