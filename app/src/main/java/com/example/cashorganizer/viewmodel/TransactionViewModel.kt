package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.data.repository.TransactionRepository
import com.example.cashorganizer.utils.DateUtils
import com.example.cashorganizer.utils.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _period = MutableStateFlow(Period.ALL)
    val period: StateFlow<Period> = _period.asStateFlow()

    val filteredTransactionFlow: StateFlow<List<TransactionEntity>> =
        _period.flatMapLatest { p ->
        val (start, end) = DateUtils.rangeForPeriod(p)
            repo.getByDateRangeFlow(start, end)
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredBalanceFlow = filteredTransactionFlow.map { list ->
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        income - expense
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun setPeriod(p: Period) {
        _period.value = p
    }

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