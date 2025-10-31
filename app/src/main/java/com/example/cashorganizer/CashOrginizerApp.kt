package com.example.cashorganizer

import android.app.Application
import com.example.cashorganizer.data.db.AppDatabase

class CashOrginizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация базы данных (лениво в getInstance)
        AppDatabase.getInstance(this)
    }
}