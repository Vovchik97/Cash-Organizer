package com.example.cashorganizer.ui.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.ui.components.BalanceCard
import com.example.cashorganizer.ui.components.TransactionItem
import com.example.cashorganizer.utils.Period
import com.example.cashorganizer.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        Period.ALL -> "Всё время"
        Period.TODAY -> "Сегодня"
        Period.WEEK -> "Неделя"
        Period.MONTH -> "Месяц"
        Period.YEAR -> "Год"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cash Organizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Карточка баланса - всегда показывает общий баланс
            BalanceCard(balance = overallBalance)

            Spacer(modifier = Modifier.height(16.dp))

            // Статистика и фильтры - показывают отфильтрованные данные
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Баланс за $periodLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.2f", balanceState)} ₽",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (balanceState >= 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        // Кнопка фильтра с иконкой
                        Box {
                            FilterChip(
                                selected = false,
                                onClick = { filteredExpanded = true },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(periodLabel)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )

                            DropdownMenu(
                                expanded = filteredExpanded,
                                onDismissRequest = { filteredExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Всё время") },
                                    onClick = {
                                        viewModel.setPeriod(Period.ALL);
                                        filteredExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Сегодня") },
                                    onClick = {
                                        viewModel.setPeriod(Period.TODAY);
                                        filteredExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Неделя") },
                                    onClick = {
                                        viewModel.setPeriod(Period.WEEK);
                                        filteredExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Месяц") },
                                    onClick = {
                                        viewModel.setPeriod(Period.MONTH);
                                        filteredExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Год") },
                                    onClick = {
                                        viewModel.setPeriod(Period.YEAR);
                                        filteredExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Дополнительная информация по фильтру
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок последних операций
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Операции за $periodLabel",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    "${transactionsState.size} операций",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список операций
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(
                    items = transactionsState,
                    key = { it.id ?: 0L }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onDelete(transaction) },
                        onEdit = { onEdit(transaction) }
                    )
                }

                item {
                    if (transactionsState.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Нет операций за $periodLabel",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Нажмите + чтобы добавить операцию",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}