package com.example.cashorganizer.ui.screen.goals

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
import com.example.cashorganizer.data.model.GoalEntity
import com.example.cashorganizer.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalViewModel = viewModel()) {
    val goals by viewModel.goalsFlow.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var showAddAmountFor: GoalEntity? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Цели") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text("Мои цели", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(goals) { g ->
                    GoalRow(
                        goal = g,
                        onEdit = { editGoal = it },
                        onDelete = { viewModel.deleteGoal(it) },
                        onAddAmount = { showAddAmountFor = it }
                    )
                    Divider()
                }
            }
        }
    }

    if (showAdd) {
        AddEditGoalDialog(
            onSave = { name, target, date ->
                viewModel.addGoal(name, target, date)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }

    if (editGoal != null) {
        AddEditGoalDialog(
            goal = editGoal,
            onSave = { name, target, date ->
                val g = editGoal!!.copy(name = name, targetAmount = target, targetDate = date)
                viewModel.updateGoal(g)
                editGoal = null
            },
            onDismiss = { editGoal = null }
        )
    }

    if (showAddAmountFor != null) {
        AddToGoalDialog(
            goal = showAddAmountFor!!,
            onAdd = { amount ->
                viewModel.addToCurrent(showAddAmountFor!!.id, amount)
                showAddAmountFor = null
            },
            onDismiss = { showAddAmountFor = null }
        )
    }
}

@Composable
fun GoalRow(
    goal: GoalEntity,
    onEdit: (GoalEntity) -> Unit,
    onDelete: (GoalEntity) -> Unit,
    onAddAmount: (GoalEntity) -> Unit
) {
    val progress = if (goal.targetAmount <= 0.0) 0f
    else (goal.currentAmount / goal.targetAmount).toFloat().coerceAtMost(1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Цель: ${String.format("%.2f", goal.targetAmount)}   " +
                            "Собрано: ${String.format("%.2f", goal.currentAmount)}"
                )
            }

            // 🔥 ЖЁСТКИЙ СТОЛБИК КНОПОК — МАЛЕНЬКИЕ, ДРУГ ПОД ДРУГОМ
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddAmount(goal) },
                    modifier = Modifier.width(70.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Взнос", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onEdit(goal) },
                    modifier = Modifier.width(70.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Ред.", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onDelete(goal) },
                    modifier = Modifier.width(70.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Уд.", fontSize = 12.sp)
                }
            }
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AddEditGoalDialog(
    goal: GoalEntity? = null,
    onSave: (String, Double, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetText by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Добавить цель" else "Редактировать цель") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("Сумма цели") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = targetText.toDoubleOrNull() ?: 0.0
                onSave(name, t, goal?.targetDate)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun AddToGoalDialog(goal: GoalEntity, onAdd: (Double) -> Unit, onDismiss: () -> Unit) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить к цели: ${goal.name}") },
        text = {
            OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Сумма") })
        },
        confirmButton = {
            TextButton(onClick = {
                val v = amountText.toDoubleOrNull() ?: 0.0
                onAdd(v)
            }) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}