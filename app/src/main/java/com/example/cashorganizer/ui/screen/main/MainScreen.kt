package com.example.cashorganizer.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.ui.components.BalanceCard
import com.example.cashorganizer.ui.components.TransactionItem
import com.example.cashorganizer.utils.Period
import com.example.cashorganizer.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    onAddClick: () -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onEdit: (TransactionEntity) -> Unit
) {
    val transactionsState by viewModel.filteredTransactionFlow.collectAsState()
    val balanceState by viewModel.filteredBalanceFlow.collectAsState()
    val overallBalance by viewModel.balanceFlow.collectAsState()
    val currentPeriod by viewModel.period.collectAsState()

    var filteredExpanded by remember { mutableStateOf(false) }

    val periodLabel = when (currentPeriod) {
        Period.ALL -> "Всё"
        Period.TODAY -> "Сегодня"
        Period.WEEK -> "Неделя"
        Period.MONTH -> "Месяц"
        Period.YEAR -> "Год"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Cash Organizer") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            BalanceCard(balance = balanceState)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Баланс за: $periodLabel (Всего: ${String.format("%.2f", overallBalance)})",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Период:", modifier = Modifier.padding(end = 8.dp))
                Box {
                    Button(onClick = { filteredExpanded = true }) {
                        Text(periodLabel)
                    }
                    DropdownMenu(
                        expanded = filteredExpanded,
                        onDismissRequest = { filteredExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Всё") }, onClick = {
                            viewModel.setPeriod(Period.ALL); filteredExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Сегодня") }, onClick = {
                            viewModel.setPeriod(Period.TODAY); filteredExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Неделя") }, onClick = {
                            viewModel.setPeriod(Period.WEEK); filteredExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Месяц") }, onClick = {
                            viewModel.setPeriod(Period.MONTH); filteredExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Год") }, onClick = {
                            viewModel.setPeriod(Period.YEAR); filteredExpanded = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Последние операции",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactionsState) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onDelete(transaction) },
                        onEdit = { onEdit(transaction) }
                    )
                }
            }
        }
    }
}