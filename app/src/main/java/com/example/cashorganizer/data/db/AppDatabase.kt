package com.example.cashorganizer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cashorganizer.data.dao.LimitsDao
import com.example.cashorganizer.data.dao.CategoryDao
import com.example.cashorganizer.data.dao.GoalDao
import com.example.cashorganizer.data.dao.TransactionDao
import com.example.cashorganizer.data.model.LimitsEntity
import com.example.cashorganizer.data.model.CategoryEntity
import com.example.cashorganizer.data.model.GoalEntity
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TransactionEntity::class, CategoryEntity::class, LimitsEntity::class, GoalEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun limitsDao(): LimitsDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_organizer_db"
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

                // Очищаем старые категории и добавляем новые с русскими названиями
                categoryDao.deleteAll()

                val defaultCategories = listOf(
                    // Доходы
                    CategoryEntity(name = "Зарплата", type = TransactionType.INCOME),
                    CategoryEntity(name = "Подарки", type = TransactionType.INCOME),
                    CategoryEntity(name = "Инвестиции", type = TransactionType.INCOME),

                    // Расходы
                    CategoryEntity(name = "Продукты", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Транспорт", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Развлечения", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Связь", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Образование", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Подарки", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Путешествия", type = TransactionType.EXPENSE),
                    CategoryEntity(name = "Бытовые расходы", type = TransactionType.EXPENSE),
                )

                defaultCategories.forEach { category ->
                    categoryDao.insert(category)
                }

                // Обновляем существующие транзакции с новыми названиями категорий
                val existingTransactions = transactionDao.getAllOnce()
                val categoryMap = mapOf(
                    "Salary" to "Зарплата",
                    "Gifts" to "Подарки",
                    "Groceries" to "Продукты",
                    "Transport" to "Транспорт",
                    "Entertainment" to "Развлечения"
                )

                existingTransactions.forEach { transaction ->
                    val newCategory = categoryMap[transaction.category] ?: transaction.category
                    if (newCategory != transaction.category) {
                        transactionDao.update(
                            transaction.copy(category = newCategory)
                        )
                    }
                }
            }
        }
    }
}