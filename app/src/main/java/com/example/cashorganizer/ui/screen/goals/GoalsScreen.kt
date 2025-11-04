/*
package com.example.cashorganizer.ui.screen.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.OutlinedButtonDefaults
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Цели", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Мои цели", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            if (goals.isEmpty()) {
                Text(
                    "Нет целей.\nНажмите +, чтобы создать первую!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(goals) { g ->
                        GoalRow(
                            goal = g,
                            onEdit = { editGoal = it },
                            onDelete = { viewModel.deleteGoal(it) },
                            onAddAmount = { showAddAmountFor = it }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                                "Собрано: ${String.format("%.2f", goal.currentAmount)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onAddAmount(goal) },
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Взнос", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Button(
                        onClick = { onEdit(goal) },
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Ред.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    OutlinedButton(
                        onClick = { onDelete(goal) },
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Уд.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Сумма цели") },
                    modifier = Modifier.fillMaxWidth()
                )
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
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Сумма") },
                modifier = Modifier.fillMaxWidth()
            )
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
}*/

package com.example.cashorganizer.ui.screen.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Цели",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
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
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Goal",
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
                .padding(16.dp)
        ) {
            // Заголовок с иконкой
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Мои цели",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            if (goals.isEmpty()) {
                // Пустое состояние
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Нет целей",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Создайте свою первую цель накопления",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals) { goal ->
                        GoalCard(
                            goal = goal,
                            onEdit = { editGoal = it },
                            onDelete = { viewModel.deleteGoal(it) },
                            onAddAmount = { showAddAmountFor = it }
                        )
                    }
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
fun GoalCard(
    goal: GoalEntity,
    onEdit: (GoalEntity) -> Unit,
    onDelete: (GoalEntity) -> Unit,
    onAddAmount: (GoalEntity) -> Unit
) {
    val progress = if (goal.targetAmount <= 0.0) 0f
    else (goal.currentAmount / goal.targetAmount).toFloat().coerceAtMost(1f)

    val progressColor = when {
        progress >= 1f -> Color(0xFF4CAF50) // Зеленый при достижении цели
        progress >= 0.7f -> Color(0xFFFF9800) // Оранжевый при близком достижении
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок и кнопки действий
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    goal.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Компактные иконки вместо кнопок
                Row {
                    IconButton(
                        onClick = { onAddAmount(goal) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Amount",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onEdit(goal) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(goal) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Прогресс и суммы
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Собрано: ${String.format("%.2f", goal.currentAmount)} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Цель: ${String.format("%.2f", goal.targetAmount)} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Прогресс-бар
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Процент выполнения
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = progressColor
                    )

                    val remaining = goal.targetAmount - goal.currentAmount
                    if (remaining > 0) {
                        Text(
                            "Осталось: ${String.format("%.2f", remaining)} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "Цель достигнута! 🎉",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
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
        title = {
            Text(
                if (goal == null) "Новая цель" else "Редактировать цель",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название цели") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Сумма цели") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val t = targetText.toDoubleOrNull() ?: 0.0
                    onSave(name, t, goal?.targetDate)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Сохранить",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddToGoalDialog(goal: GoalEntity, onAdd: (Double) -> Unit, onDismiss: () -> Unit) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Добавить к цели",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    goal.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Сумма взноса") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val v = amountText.toDoubleOrNull() ?: 0.0
                    onAdd(v)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Добавить",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}