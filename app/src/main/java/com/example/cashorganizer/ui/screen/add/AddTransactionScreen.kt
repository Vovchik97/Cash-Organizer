/*
// 📁 AddTransactionScreen.kt
package com.example.cashorganizer.ui.screen.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.ui.components.AddCategoryDialog
import com.example.cashorganizer.viewmodel.CategoryViewModel
import com.example.cashorganizer.viewmodel.TransactionViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: TransactionViewModel, onBack: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val categoryViewModel: CategoryViewModel = viewModel()
    val categoriesState by categoryViewModel.categoriesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Новая операция", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Сумма") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categoriesState
                        .filter { it.type == type }
                        .distinctBy { it.name }
                        .forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryName = cat.name
                                    expanded = false
                                }
                            )
                        }
                    DropdownMenuItem(
                        text = { Text("➕ Добавить категорию") },
                        onClick = {
                            showAddCategoryDialog = true
                            expanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (опционально)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Тип операции:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            SegmentedTypeSelector(selected = type, onSelect = { type = it })
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        viewModel.addTransaction(
                            amount = amount,
                            type = type,
                            category = selectedCategoryName.ifBlank { "Без категорий" },
                            date = Date().time,
                            note = note.ifBlank { null }
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Сохранить")
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = type,
            onAdd = { name ->
                categoryViewModel.addCategory(name, type)
                selectedCategoryName = name
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}

@Composable
fun SegmentedTypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row {
        Button(
            onClick = { onSelect(TransactionType.EXPENSE) },
            colors = if (selected == TransactionType.EXPENSE)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            else
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Расход")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onSelect(TransactionType.INCOME) },
            colors = if (selected == TransactionType.INCOME)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            else
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Доход")
        }
    }
}*/


package com.example.cashorganizer.ui.screen.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.ui.components.AddCategoryDialog
import com.example.cashorganizer.ui.screen.edit.CategoryDropdown
import com.example.cashorganizer.ui.screen.edit.SegmentedTypeSelector
import com.example.cashorganizer.viewmodel.CategoryViewModel
import com.example.cashorganizer.viewmodel.TransactionViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: TransactionViewModel, onBack: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val categoryViewModel: CategoryViewModel = viewModel()
    val categoriesState by categoryViewModel.categoriesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Новая операция",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Сумма
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Сумма") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("0.00") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Тип операции
                    Text(
                        "Тип операции",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SegmentedTypeSelector(selected = type, onSelect = { type = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    // Категория
                    Text(
                        "Категория",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryDropdown(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        selectedCategoryName = selectedCategoryName,
                        categoriesState = categoriesState,
                        type = type,
                        onCategorySelected = { selectedCategoryName = it },
                        onAddCategory = { showAddCategoryDialog = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Заметка
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Заметка (необязательно)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка сохранения
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        viewModel.addTransaction(
                            amount = amount,
                            type = type,
                            category = selectedCategoryName.ifBlank { "Без категории" },
                            date = Date().time,
                            note = note.ifBlank { null }
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = amountText.isNotEmpty() && amountText.toDoubleOrNull() ?: 0.0 > 0
            ) {
                Text(
                    "Добавить операцию",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = type,
            onAdd = { name ->
                categoryViewModel.addCategory(name, type)
                selectedCategoryName = name
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}