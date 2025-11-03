package com.example.cashorganizer.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cashorganizer.ui.screen.add.AddTransactionScreen
import com.example.cashorganizer.ui.screen.analytics.AnalyticsScreen
import com.example.cashorganizer.ui.screen.budget.BudgetScreen
import com.example.cashorganizer.ui.screen.edit.EditTransactionScreen
import com.example.cashorganizer.ui.screen.goals.GoalsScreen
import com.example.cashorganizer.ui.screen.main.MainScreen
import com.example.cashorganizer.viewmodel.AnalyticsViewModel
import com.example.cashorganizer.viewmodel.BudgetViewModel
import com.example.cashorganizer.viewmodel.GoalViewModel
import com.example.cashorganizer.viewmodel.TransactionViewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AddTransaction : Screen("add")
    object EditTransaction : Screen("edit/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit/$transactionId"
    }
    object Analytics : Screen("analytics")
    object Budget : Screen("budget")
    object Goals : Screen("goals")
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Главная") },
                    selected = currentDestination == Screen.Main.route,
                    onClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Аналитика") },
                    selected = currentDestination == Screen.Analytics.route,
                    onClick = {
                        navController.navigate(Screen.Analytics.route) {
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Бюджет") },
                    selected = currentDestination == Screen.Budget.route,
                    onClick = {
                        navController.navigate(Screen.Budget.route) {
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Цели") },
                    selected = currentDestination == Screen.Goals.route,
                    onClick = {
                        navController.navigate(Screen.Goals.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                val viewModel: TransactionViewModel = viewModel()
                MainScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Screen.AddTransaction.route) },
                    onDelete = { transaction ->
                        viewModel.deleteTransaction(transaction)
                    },
                    onEdit = { transaction ->
                        navController.navigate(Screen.EditTransaction.createRoute(transaction.id))
                    }
                )
            }

            composable(Screen.AddTransaction.route) {
                val viewModel: TransactionViewModel = viewModel()
                AddTransactionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: TransactionViewModel = viewModel()
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                val transaction = viewModel.transactionFlow.collectAsState().value.find { it.id == transactionId }
                if (transaction != null) {
                    EditTransactionScreen(
                        viewModel = viewModel,
                        transaction = transaction,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Analytics.route) {
                val analyticsViewModel: AnalyticsViewModel = viewModel()
                AnalyticsScreen(viewModel = analyticsViewModel)
            }

            composable(Screen.Budget.route) {
                val budgetViewModel: BudgetViewModel = viewModel()
                BudgetScreen(viewModel = budgetViewModel)
            }

            composable(Screen.Goals.route) {
                val goalsViewModel: GoalViewModel = viewModel()
                GoalsScreen(viewModel = goalsViewModel)
            }
        }
    }
}