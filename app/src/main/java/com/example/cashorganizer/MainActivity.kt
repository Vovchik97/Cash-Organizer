package com.example.cashorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.cashorganizer.ui.screen.MainNavHost
import com.example.cashorganizer.ui.theme.CashOrganizerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CashOrganizerTheme {
                Surface {
                    MainNavHost()
                }
            }
        }
    }
}