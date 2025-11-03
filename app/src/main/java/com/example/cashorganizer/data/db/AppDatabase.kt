package com.example.cashorganizer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cashorganizer.data.dao.BudgetDao
import com.example.cashorganizer.data.dao.CategoryDao
import com.example.cashorganizer.data.dao.GoalDao
import com.example.cashorganizer.data.dao.TransactionDao
import com.example.cashorganizer.data.model.BudgetEntity
import com.example.cashorganizer.data.model.CategoryEntity
import com.example.cashorganizer.data.model.GoalEntity
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class, GoalEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_orginizer_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
                prepopulate(instance)
                instance
            }
        }

        private fun prepopulate(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val transactionDao = db.transactionDao()
                val categoryDao = db.categoryDao()

                val existingTransactions = transactionDao.getAllOnce()
                if (existingTransactions.isEmpty()) {
                    transactionDao.insert(
                        TransactionEntity(
                            amount = 1500.0,
                            type = TransactionType.INCOME,
                            category = "Salary",
                            date = System.currentTimeMillis(),
                            note = "First salary"
                        )
                    )
                    transactionDao.insert(
                        TransactionEntity(
                            amount = 200.0,
                            type = TransactionType.EXPENSE,
                            category = "Groceries",
                            date = System.currentTimeMillis(),
                            note = "Supermarket"
                        )
                    )
                }

                val existingCategories = categoryDao.getAllOnce()
                if (existingCategories.isEmpty())
                {
                    val defaultCategories = listOf(
                        CategoryEntity(name = "Salary", type = TransactionType.INCOME),
                        CategoryEntity(name = "Gifts", type = TransactionType.INCOME),
                        CategoryEntity(name = "Groceries", type = TransactionType.EXPENSE),
                        CategoryEntity(name = "Transport", type = TransactionType.EXPENSE),
                        CategoryEntity(name = "Entertainment", type = TransactionType.EXPENSE)
                    )
                    defaultCategories.forEach { category ->
                        categoryDao.insert(category)
                    }
                }
            }
        }
    }
}