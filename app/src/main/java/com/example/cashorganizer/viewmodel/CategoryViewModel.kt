package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.CategoryEntity
import com.example.cashorganizer.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).categoryDao()
    private val repo = CategoryRepository(dao)

    val categoriesFlow = repo.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, type: com.example.cashorganizer.data.model.TransactionType) {
        viewModelScope.launch {
            repo.insert(CategoryEntity(name = name, type = type))
        }
    }
}