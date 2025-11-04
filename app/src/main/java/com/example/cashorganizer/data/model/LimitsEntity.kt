package com.example.cashorganizer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "limits") // Меняем на limits
data class LimitsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val month: String,
    val limitAmount: Double,
    val periodType: String = "MONTHLY", // Добавляем тип периода
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)