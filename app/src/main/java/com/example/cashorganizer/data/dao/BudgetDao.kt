package com.example.cashorganizer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cashorganizer.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY month DESC")
    fun getAllFlow(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month LIMIT 1")
    suspend fun getByCategoryAndMonth(categoryId: Long, month: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE month = :month ORDER BY categoryId ASC")
    suspend fun getByMonthOnce(month: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsForMonthFlow(month: String): Flow<List<BudgetEntity>>
}