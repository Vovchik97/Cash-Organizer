package com.example.cashorganizer.ui.screen.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashorganizer.viewmodel.BudgetUiModel
import com.example.cashorganizer.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel = viewModel()) {
    val items by viewModel.budgetUiModels.collectAsState()
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val currentMonth = sdf.format(Date())

    var showSetDialog by remember { mutableStateOf(false) }
    var dialogCategoryId by remember { mutableStateOf<Long?>(null) }
    var dialogCategoryName by remember { mutableStateOf("") }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Бюджеты — $currentMonth") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text("Бюджеты по категориям", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(items) { model ->
                    BudgetRow(
                        model = model,
                        onSetLimit = { id, name ->
                            dialogCategoryId = id
                            dialogCategoryName = name
                            showSetDialog = true
                        },
                        onDeleteBudget = { m ->
                            m?.budget?.let { viewModel.deleteBudget(it) }
                        }
                    )
                    Divider()
                }
            }
        }
    }

    if (showSetDialog && dialogCategoryId != null) {
        SetBudgetDialog(
            categoryId = dialogCategoryId!!,
            categoryName = dialogCategoryName,
            month = currentMonth,
            onConfirm = { limit ->
                viewModel.setBudgetForCategory(dialogCategoryId!!, currentMonth, limit)
                showSetDialog = false
            },
            onDismiss = { showSetDialog = false }
        )
    }
}

@Composable
fun BudgetRow(
    model: BudgetUiModel,
    onSetLimit: (Long, String) -> Unit,
    onDeleteBudget: (BudgetUiModel?) -> Unit
) {
    val progress = (model.limitAmount.takeIf { it > 0 }
        ?.let { (model.spentAmount / it).toFloat().coerceAtMost(1f) } ?: 0f)

    // Проверка превышения лимита
    val isOverBudget = model.limitAmount > 0 && model.spentAmount > model.limitAmount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    model.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Потрачено: ${String.format("%.2f", model.spentAmount)} / ${String.format("%.2f", model.limitAmount)}",
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
                if (isOverBudget) {
                    Text(
                        "⚠️ ПРЕВЫШЕНИЕ ЛИМИТА НА ${String.format("%.2f", model.spentAmount - model.limitAmount)}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = { onSetLimit(model.categoryId!!, model.categoryName) },
                    modifier = Modifier.width(70.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (model.limitAmount > 0.0) "Ред." else "Задать", fontSize = 12.sp)
                }
                if (model.budget != null) {
                    OutlinedButton(
                        onClick = { onDeleteBudget(model) },
                        modifier = Modifier.width(70.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Уд.", fontSize = 12.sp)
                    }
                }
            }
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = when {
                isOverBudget -> MaterialTheme.colorScheme.error
                progress > 0.8f -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.primary
            }
        )
        Text("${model.percent}%", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SetBudgetDialog(
    categoryId: Long,
    categoryName: String,
    month: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Установить лимит") },
        text = {
            Column {
                Text("Категория: $categoryName")
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Лимит (число)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.toDoubleOrNull() ?: 0.0
                onConfirm(v)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}