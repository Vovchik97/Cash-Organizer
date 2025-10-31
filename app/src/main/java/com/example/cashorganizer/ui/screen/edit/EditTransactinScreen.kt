package com.example.cashorganizer.ui.screen.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cashorganizer.data.model.TransactionEntity
import com.example.cashorganizer.data.model.TransactionType
import com.example.cashorganizer.viewmodel.TransactionViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    viewModel: TransactionViewModel,
    transaction: TransactionEntity,
    onBack: () -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var type by remember { mutableStateOf(transaction.type) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Редактировать операцию") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Сумма") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Категория") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (опционально)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Тип: ", modifier = Modifier.padding(end = 16.dp))
                SegmentedTypeSelector(selected = type, onSelect = { type = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0 && category.isNotBlank()) {
                    val updatedTransaction = transaction.copy(
                        amount = amount,
                        type = type,
                        category = if (category.isBlank()) "Без категорий" else category,
                        note = note.ifBlank { null }
                    )
                    viewModel.updateTransaction(updatedTransaction)
                    onBack()
                } else {
                    // можно показать Snackbar — для простоты пока ничего
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
fun SegmentedTypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row {
        Button(
            onClick = { onSelect(TransactionType.EXPENSE) },
            colors = if (selected == TransactionType.EXPENSE) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Расход")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onSelect(TransactionType.INCOME) },
            colors = if (selected == TransactionType.INCOME) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Доход")
        }
    }
}