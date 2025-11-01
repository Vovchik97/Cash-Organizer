package com.example.cashorganizer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cashorganizer.data.model.CategoryEntity

@Composable
fun CategoryPickerDialog(
    categories: List<CategoryEntity>,
    onSelect: (CategoryEntity) -> Unit,
    onDismis: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismis,
        confirmButton = {
            Button(onClick = onDismis) {
                Text("Закрыть")
            }
        },
        title = { Text("Выберите категорию") },
        text = {
            Surface {
                LazyColumn {
                    items(categories) { cat ->
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(cat) }
                            .padding(12.dp)) {
                            Text(text = cat.name)
                        }
                    }
                }
            }
        }
    )
}