package com.example.cashorganizer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cashorganizer.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) FROM transactions")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) FROM transactions")
    suspend fun getTotalExpense(): Double?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?
}