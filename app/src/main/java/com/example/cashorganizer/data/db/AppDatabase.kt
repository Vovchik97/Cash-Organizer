package com.example.cashorganizer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cashorganizer.data.dao.TransactionDao
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

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

                instance
            }
        }

        private fun prepopulate(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = db.transactionDao()
                dao.insert(
                    TransactionEntity(
                        amount = 1500.0,
                        type = TransactionType.INCOME,
                        category = "Salary",
                        date = System.currentTimeMillis(),
                        note = "First salary"
                    )
                )
                dao.insert(
                    TransactionEntity(
                        amount = 200.0,
                        type = TransactionType.EXPENSE,
                        category = "Groceries",
                        date = System.currentTimeMillis(),
                        note = "Supermarket"
                    )
                )
            }
        }
    }
}