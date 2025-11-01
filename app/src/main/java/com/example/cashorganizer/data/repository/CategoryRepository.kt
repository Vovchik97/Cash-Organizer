package com.example.cashorganizer.data.repository

import com.example.cashorganizer.data.dao.CategoryDao
import com.example.cashorganizer.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {

    fun getAllFlow(): Flow<List<CategoryEntity>> = dao.getAllFlow()

    suspend fun insert(category: CategoryEntity): Long {
        dao.insert(category)
        return 0L
    }

    suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)
}