package com.example.cashorganizer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashorganizer.data.db.AppDatabase
import com.example.cashorganizer.data.model.GoalEntity
import com.example.cashorganizer.data.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = GoalRepository(db.goalDao())

    val goalsFlow = repo.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addGoal(name: String, target: Double, targetDate: Long?) {
        viewModelScope.launch {
            repo.insert(GoalEntity(name = name, targetAmount = target, targetDate = targetDate))
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repo.update(goal.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repo.delete(goal)
        }
    }

    fun addToCurrent(goalId: Long, amount: Double) {
        viewModelScope.launch {
            val g = repo.getById(goalId) ?: return@launch
            val newAmount = (g.currentAmount + amount).coerceAtLeast(0.0)
            repo.update(g.copy(currentAmount = newAmount, updatedAt = System.currentTimeMillis()))
        }
    }
}