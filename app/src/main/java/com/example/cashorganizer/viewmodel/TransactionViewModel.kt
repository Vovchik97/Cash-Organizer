package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel (application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).transactionDao()
    private val repo = TransactionRepository(dao)

    val transactionFlow = repo.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balanceFlow = repo.getAllFlow().map { list ->
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount}
        val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount}
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(amount: Double, type: TransactionType, category: String, date: Long, note: String?) {
        viewModelScope.launch {
            val t = TransactionEntity(
                amount = amount,
                type = type,
                category = category,
                date = date,
                note = note
            )
            repo.insert(t)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repo.update(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repo.delete(transaction)
        }
    }
}