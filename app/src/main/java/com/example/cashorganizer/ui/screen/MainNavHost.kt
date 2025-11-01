package com.example.cashorganizer.ui.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.cashorganizer.ui.screen.add.AddTransactionScreen
import com.example.cashorganizer.ui.screen.edit.EditTransactionScreen
import com.example.cashorganizer.ui.screen.main.MainScreen
import com.example.cashorganizer.viewmodel.TransactionViewModel
import androidx.compose.runtime.collectAsState


sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AddTransaction : Screen("add")
    object EditTransaction: Screen("edit/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit/$transactionId"
    }
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val viewModel: TransactionViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
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
            AddTransactionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: - 1L
            val transaction = viewModel.transactionFlow.collectAsState().value.find { it.id == transactionId }
            if (transaction != null) {
                EditTransactionScreen(
                    viewModel = viewModel,
                    transaction = transaction,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}