package com.example.cashorganizer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cashorganizer.data.model.LimitsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LimitsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: LimitsEntity): Long

    @Update
    suspend fun update(budget: LimitsEntity)

    @Delete
    suspend fun delete(budget: LimitsEntity)

    @Query("SELECT * FROM limits ORDER BY month DESC")
    fun getAllFlow(): Flow<List<LimitsEntity>>

    @Query("SELECT * FROM limits WHERE categoryId = :categoryId AND month = :month LIMIT 1")
    suspend fun getByCategoryAndMonth(categoryId: Long, month: String): LimitsEntity?

    @Query("SELECT * FROM limits WHERE month = :month ORDER BY categoryId ASC")
    suspend fun getByMonthOnce(month: String): List<LimitsEntity>

    @Query("SELECT * FROM limits WHERE month = :month")
    fun getLimitsForMonthFlow(month: String): Flow<List<LimitsEntity>>
}