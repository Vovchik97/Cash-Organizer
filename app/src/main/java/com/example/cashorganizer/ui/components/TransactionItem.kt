package com.example.cashorganizer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Delete
import com.example.cashorganizer.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onEdit() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.category, style = MaterialTheme.typography.titleSmall)
            Text(text = transaction.note ?: "", style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (transaction.type.name == "INCOME") "+" else "-") + String.format("%.2f", transaction.amount),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = sdf.format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete, modifier = Modifier.padding(start = 8.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}