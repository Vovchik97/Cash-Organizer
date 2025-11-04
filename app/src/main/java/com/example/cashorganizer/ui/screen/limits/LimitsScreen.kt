package com.example.cashorganizer.ui.screen.limits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import com.example.cashorganizer.viewmodel.LimitsUiModel
import com.example.cashorganizer.viewmodel.LimitsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen(viewModel: LimitsViewModel = viewModel()) {
    val items by viewModel.limitsUiModels.collectAsState()

    // Функция для получения названия месяца в именительном падеже
    fun getMonthNameInNominative(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val monthNames = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )

        return "${monthNames[month]} $year"
    }

    val currentMonthDisplay = getMonthNameInNominative()
    val currentMonthApi = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    var showSetDialog by remember { mutableStateOf(false) }
    var dialogCategoryId by remember { mutableStateOf<Long?>(null) }
    var dialogCategoryName by remember { mutableStateOf("") }
    var selectedPeriodType by remember { mutableStateOf("MONTHLY") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Лимиты",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
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
        ) {
            // Заголовок месяца
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Лимиты на $currentMonthDisplay",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "Управление лимитами расходов по категориям",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Информация о периодах
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💡 Траты автоматически сбрасываются в начале каждого периода",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }

            // Информация о том, что показываются только расходные категории
            if (items.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Показываются только категории расходов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (items.isEmpty()) {
                // Пустое состояние
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Нет установленных лимитов",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Установите лимиты для отслеживания расходов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Доступны только категории расходов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { model ->
                        LimitCard(
                            model = model,
                            onSetLimit = { id, name ->
                                dialogCategoryId = id
                                dialogCategoryName = name
                                selectedPeriodType = model.periodType
                                showSetDialog = true
                            },
                            onDeleteLimit = { m ->
                                m?.limit?.let { viewModel.deleteLimit(it) }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSetDialog && dialogCategoryId != null) {
        SetLimitDialog(
            categoryId = dialogCategoryId!!,
            categoryName = dialogCategoryName,
            currentPeriodType = selectedPeriodType,
            onConfirm = { limit, periodType ->
                viewModel.setLimitForCategory(dialogCategoryId!!, currentMonthApi, limit, periodType)
                showSetDialog = false
                dialogCategoryId = null
                dialogCategoryName = ""
            },
            onDismiss = {
                showSetDialog = false
                dialogCategoryId = null
                dialogCategoryName = ""
            }
        )
    }
}

@Composable
fun LimitCard(
    model: LimitsUiModel,
    onSetLimit: (Long, String) -> Unit,
    onDeleteLimit: (LimitsUiModel?) -> Unit
) {
    val progress = (model.limitAmount.takeIf { it > 0 }
        ?.let { (model.spentAmount / it).toFloat().coerceAtMost(1f) } ?: 0f)

    val isOverLimit = model.limitAmount > 0 && model.spentAmount > model.limitAmount
    val isNearLimit = model.limitAmount > 0 && model.spentAmount >= model.limitAmount * 0.8f && !isOverLimit

    val progressColor = when {
        isOverLimit -> MaterialTheme.colorScheme.error
        isNearLimit -> Color(0xFFFF9800)
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
            // Заголовок и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        model.categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isOverLimit) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onBackground
                    )

                    // Информация о периоде
                    Text(
                        "Период: ${getPeriodDisplayName(model.periodType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Потрачено: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${String.format("%.2f", model.spentAmount)} ₽",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isOverLimit) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            " / ${String.format("%.2f", model.limitAmount)} ₽",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Статус иконка
                if (isOverLimit) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Превышение лимита",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Предупреждение о превышении
            if (isOverLimit) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ Превышение на ${String.format("%.2f", model.spentAmount - model.limitAmount)} ₽",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            // Нижняя строка с процентом и кнопками
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${model.percent}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = progressColor
                )

                Row {
                    FilledTonalButton(
                        onClick = { onSetLimit(model.categoryId!!, model.categoryName) },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (model.limitAmount > 0.0) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            if (model.limitAmount > 0.0) "Изменить" else "Задать",
                            fontSize = 12.sp
                        )
                    }

                    if (model.limit != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onDeleteLimit(model) },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                "Удалить",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetLimitDialog(
    categoryId: Long,
    categoryName: String,
    currentPeriodType: String,
    onConfirm: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedPeriodType by remember { mutableStateOf(currentPeriodType) }

    // Функция для проверки ввода
    fun validateInput(input: String): Boolean {
        if (input.isBlank()) {
            isError = true
            errorMessage = "Введите сумму лимита"
            return false
        }

        val amount = input.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            isError = true
            errorMessage = "Введите корректную сумму больше 0"
            return false
        }

        isError = false
        errorMessage = ""
        return true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (text.isNotEmpty() && text.toDoubleOrNull() ?: 0.0 > 0) "Установить лимит" else "Новый лимит",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    "Категория: $categoryName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Выбор периода
                Text(
                    "Период лимита:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodChip(
                        periodType = "DAILY",
                        selected = selectedPeriodType == "DAILY",
                        onClick = { selectedPeriodType = "DAILY" }
                    )
                    PeriodChip(
                        periodType = "WEEKLY",
                        selected = selectedPeriodType == "WEEKLY",
                        onClick = { selectedPeriodType = "WEEKLY" }
                    )
                    PeriodChip(
                        periodType = "MONTHLY",
                        selected = selectedPeriodType == "MONTHLY",
                        onClick = { selectedPeriodType = "MONTHLY" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        // Автоматическая валидация при вводе
                        if (it.isNotEmpty()) {
                            validateInput(it)
                        } else {
                            isError = false
                            errorMessage = ""
                        }
                    },
                    label = { Text("Сумма лимита") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(errorMessage)
                        } else if (text.isNotEmpty()) {
                            Text("Лимит: ${String.format("%.2f", text.toDoubleOrNull() ?: 0.0)} ₽")
                        }
                    },
                    placeholder = { Text("0.00") }
                )

                // Подсказка о периоде
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "💡 Траты будут сбрасываться каждую ${getPeriodDisplayName(selectedPeriodType).lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validateInput(text)) {
                        val amount = text.toDoubleOrNull() ?: 0.0
                        onConfirm(amount, selectedPeriodType)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                enabled = text.isNotEmpty() && !isError
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
fun PeriodChip(
    periodType: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val displayName = getPeriodDisplayName(periodType)

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(displayName) },
        modifier = Modifier.height(36.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// Функция для отображения названия периода
private fun getPeriodDisplayName(periodType: String): String {
    return when (periodType) {
        "DAILY" -> "День"
        "WEEKLY" -> "Неделя"
        "MONTHLY" -> "Месяц"
        else -> "Месяц"
    }
}