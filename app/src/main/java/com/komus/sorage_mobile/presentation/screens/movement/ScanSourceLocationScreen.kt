package com.komus.sorage_mobile.presentation.screens.movement

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.model.ConditionReasons
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import com.komus.sorage_mobile.domain.viewModel.MovementViewModel
import com.komus.sorage_mobile.presentation.components.LoadingIndicator
import com.komus.sorage_mobile.presentation.components.ScanButton
import com.komus.sorage_mobile.presentation.theme.Primary
import com.komus.sorage_mobile.util.SPHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "ScanSourceLocationScreen"
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScanSourceLocationScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel,
    movementViewModel: MovementViewModel = hiltViewModel(),
    spHelper: SPHelper
) {
    var locationId by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val locationItemsState by movementViewModel.locationItemsState.collectAsStateWithLifecycle()
    val moveProductState by movementViewModel.moveProductState.collectAsStateWithLifecycle()

    var selectedItem by remember { mutableStateOf<LocationItem?>(null) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showExpirationAndConditionDialog by remember { mutableStateOf(false) }
    var showTargetLocationDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var moveQuantity by remember { mutableStateOf(0) }
    var targetLocation by remember { mutableStateOf("") }

    // Новое состояние для успешного диалога
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Обновление данных при возврате на экран
    LaunchedEffect(Unit) {
        if (locationId.isNotEmpty()) {
            movementViewModel.getLocationItems(locationId)
        }
    }

    // Обработка состояния перемещения
    LaunchedEffect(moveProductState) {
        when (moveProductState) {
            is MovementViewModel.MoveProductState.Error -> {
                errorMessage = (moveProductState as MovementViewModel.MoveProductState.Error).message
                showErrorDialog = true
            }
            is MovementViewModel.MoveProductState.Success -> {
                // Показываем диалог об успехе
                successMessage = "Перемещение успешно выполнено"
                showSuccessDialog = true
                movementViewModel.resetMoveResult()
            }
            else -> {}
        }
    }

    // Автоматический поиск товаров после сканирования
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            if (!showTargetLocationDialog) {
                // Если не выбрана целевая ячейка, значит сканируем ячейку-источник
                locationId = barcodeData
                scannerViewModel.clearBarcode()
                movementViewModel.getLocationItems(locationId)
            } else {
                // Если `showTargetLocationDialog` открыт – сканируем целевую ячейку
                targetLocation = barcodeData
                scannerViewModel.clearBarcode()
                showTargetLocationDialog = false
                movementViewModel.setTargetLocation(targetLocation)
                movementViewModel.moveProduct()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор ячейки", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                backgroundColor = Primary,
                elevation = 4.dp,
                modifier = Modifier.height(48.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Шаг 1: Выберите ячейку",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Поле ввода ID ячейки
            OutlinedTextField(
                value = locationId,
                onValueChange = { locationId = it },
                label = { Text("ID ячейки", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск", modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (locationId.isNotEmpty()) {
                        IconButton(onClick = { locationId = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Кнопка "Найти"
            Button(
                onClick = { movementViewModel.getLocationItems(locationId) },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                enabled = locationId.isNotEmpty(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("НАЙТИ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Вывод списка товаров в ячейке
            when (locationItemsState) {
                is MovementViewModel.LocationItemsState.Success -> {
                    val items = (locationItemsState as MovementViewModel.LocationItemsState.Success).items
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            CompactProductCard(
                                item = item,
                                onSelectClick = {
                                    selectedItem = item
                                    showQuantityDialog = true
                                }
                            )
                        }
                    }
                }
                is MovementViewModel.LocationItemsState.Loading -> LoadingIndicator()
                is MovementViewModel.LocationItemsState.Error -> Text(
                    text = (locationItemsState as MovementViewModel.LocationItemsState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
                is MovementViewModel.LocationItemsState.Empty -> Text(
                    text = "Отсканируйте ячейку или введите её ID",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }

    // Диалог ввода количества
    if (showQuantityDialog && selectedItem != null) {
        CompactQuantityDialog(
            item = selectedItem!!,
            onConfirm = { quantity ->
                moveQuantity = quantity
                showQuantityDialog = false
                showExpirationAndConditionDialog = true
            },
            onDismiss = { showQuantityDialog = false }
        )
    }

    // Диалог ввода срока годности и состояния
    if (showExpirationAndConditionDialog && selectedItem != null) {
        ExpirationAndConditionDialog(
            item = selectedItem!!,
            onConfirm = { expirationDate, condition, reason ->
                movementViewModel.selectItem(selectedItem!!)
                movementViewModel.setMoveQuantity(moveQuantity)
                movementViewModel.setExpirationDate(expirationDate)
                movementViewModel.setConditionState(condition, reason)
                showExpirationAndConditionDialog = false
                showTargetLocationDialog = true
            },
            onDismiss = { showExpirationAndConditionDialog = false }
        )
    }

    // Диалог выбора целевой ячейки
    if (showTargetLocationDialog) {
        CompactTargetLocationDialog(
            onConfirm = { location ->
                targetLocation = location
                movementViewModel.setTargetLocation(targetLocation)
                showTargetLocationDialog = false
                movementViewModel.moveProduct()
            },
            onDismiss = { showTargetLocationDialog = false },
            scannerViewModel = scannerViewModel
        )
    }

    // Диалог ошибки
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                movementViewModel.resetMoveResult()
            },
            title = { 
                Text(
                    "Ошибка перемещения",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ) 
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        movementViewModel.resetMoveResult()
                    }
                ) {
                    Text("ОК", fontSize = 14.sp)
                }
            },
            shape = RoundedCornerShape(8.dp)
        )
    }

    // Диалог успеха
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    "Успех",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = successMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("ОК", fontSize = 14.sp)
                }
            },
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExpirationAndConditionDialog(
    item: LocationItem,
    onConfirm: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var finalDate by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Кондиция") }
    var reason by remember { mutableStateOf("") }
    var showReasonError by remember { mutableStateOf(false) }
    var showExpirationAlert by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var skipExpirationDate by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Функция для проверки корректности даты
    fun isValidDate(dateStr: String): Boolean {
        return try {
            val parts = dateStr.split(".")
            if (parts.size != 3) return false
            
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            
            if (day < 1 || day > 31) return false
            if (month < 1 || month > 12) return false
            if (year < 2000 || year > 2100) return false
            
            true
        } catch (e: Exception) {
            false
        }
    }

    // Функция для проверки срока годности
    fun checkExpirationDate(dateStr: String): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date = LocalDate.parse(dateStr, formatter)
            val isoDate = date.format(DateTimeFormatter.ISO_DATE)
            return ExpirationDateValidator.isExpired(isoDate)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке срока годности: ${e.message}")
            return false
        }
    }

    // Функция для расчета конечной даты
    fun calculateFinalDate() {
        if (skipExpirationDate || !isValidDate(startDate)) return
        
        try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val baseDate = LocalDate.parse(startDate, formatter)
            var calculatedDate = baseDate
            
            val monthsToAdd = months.toIntOrNull() ?: 0
            val daysToAdd = days.toIntOrNull() ?: 0
            
            if (monthsToAdd > 0) {
                calculatedDate = calculatedDate.plusMonths(monthsToAdd.toLong())
            }
            
            if (daysToAdd > 0) {
                calculatedDate = calculatedDate.plusDays(daysToAdd.toLong())
            }
            
            finalDate = calculatedDate.format(formatter)
            
            // Проверяем срок годности по итоговой дате
            if (checkExpirationDate(finalDate) && condition == "Кондиция") {
                showExpirationAlert = true
                condition = "Некондиция"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при расчете даты: ${e.message}")
        }
    }

    // Диалог предупреждения о сроке годности
    if (showExpirationAlert) {
        AlertDialog(
            onDismissRequest = { showExpirationAlert = false },
            title = { Text("Внимание!") },
            text = { 
                Column {
                    Text("Срок годности товара истек.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Для товара с истекшим сроком годности можно установить только состояние 'Некондиция'.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showExpirationAlert = false
                    }
                ) {
                    Text("ОК")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Срок годности и состояние",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                if (!skipExpirationDate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { 
                                startDate = it
                                if (isValidDate(it)) {
                                    calculateFinalDate()
                                }
                            },
                            label = { Text("Дата начала срока годности (ДД.ММ.ГГГГ)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                // Устанавливаем дату 01.01.2999 и очищаем поля
                                startDate = ""
                                days = ""
                                months = ""
                                finalDate = "01.01.2999"
                                skipExpirationDate = true
                            },
                            modifier = Modifier.height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Пропустить СГ",
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = months,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() }) {
                                    months = it
                                    if (isValidDate(startDate)) {
                                        calculateFinalDate()
                                    }
                                }
                            },
                            label = { Text("Месяцы", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = days,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() }) {
                                    days = it
                                    if (isValidDate(startDate)) {
                                        calculateFinalDate()
                                    }
                                }
                            },
                            label = { Text("Дни", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (finalDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Итоговая дата: $finalDate",
                            color = MaterialTheme.colors.primary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = "Срок годности: 01.01.2999 (пропущен)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            skipExpirationDate = false
                            finalDate = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Ввести дату вручную", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Состояние товара:",
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = condition == "Кондиция",
                            onClick = { 
                                // Проверяем по итоговой дате, если она есть
                                if (finalDate.isNotEmpty()) {
                                    if (checkExpirationDate(finalDate)) {
                                            showExpirationAlert = true
                                            return@RadioButton
                                    }
                                }
                                condition = "Кондиция"
                                showReasonError = false
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Кондиция", fontSize = 14.sp)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = condition == "Некондиция",
                            onClick = { 
                                condition = "Некондиция"
                                showReasonError = reason.isEmpty()
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Некондиция", fontSize = 14.sp)
                    }
                }

                if (condition == "Некондиция") {
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = {},
                            readOnly = true,
                            label = { 
                                Text(
                                    if (reason.isEmpty()) "Выберите причину некондиции" else "Причина некондиции",
                                    fontSize = 12.sp
                                ) 
                            },
                            trailingIcon = {
                                Icon(
                                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    "Развернуть",
                                    Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            isError = showReasonError && reason.isEmpty()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colors.surface)
                                .heightIn(max = 200.dp)
                        ) {
                            ConditionReasons.reasons.forEach { reasonOption ->
                                DropdownMenuItem(
                                    onClick = {
                                        reason = reasonOption
                                        expanded = false
                                        showReasonError = false
                                    }
                                ) {
                                    Text(
                                        text = reasonOption,
                                        fontSize = 12.sp,
                                        color = if (reason == reasonOption) 
                                            MaterialTheme.colors.primary 
                                        else 
                                            MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (showReasonError && reason.isEmpty()) {
                        Text(
                            text = "Выберите причину некондиции",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (condition == "Некондиция" && reason.isEmpty()) {
                        showReasonError = true
                        return@Button
                    }

                    val dateToUse = if (skipExpirationDate) "01.01.2999" else (if (finalDate.isNotEmpty()) finalDate else startDate)
                    if (dateToUse.isEmpty()) {
                        return@Button
                    }

                    try {
                        val isoDate = if (skipExpirationDate) {
                            "2999-01-01"
                        } else {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val date = LocalDate.parse(dateToUse, formatter)
                            date.format(DateTimeFormatter.ISO_DATE)
                        }
                        
                        // Финальная проверка по итоговой дате только если не пропущен срок годности
                        if (!skipExpirationDate && checkExpirationDate(dateToUse) && condition == "Кондиция") {
                            showExpirationAlert = true
                            return@Button
                        }
                        
                        onConfirm(isoDate, condition, if (condition == "Некондиция") reason else null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при сохранении даты: ${e.message}")
                    }
                },
                enabled = (skipExpirationDate || startDate.isNotEmpty() || finalDate.isNotEmpty()) && 
                         (condition != "Некондиция" || reason.isNotEmpty())
            ) {
                Text("ОК", fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", fontSize = 14.sp)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun CompactQuantityDialog(
    item: LocationItem,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    val availableQuantity = item.units.firstOrNull()?.quantity?.toIntOrNull() ?: 0
    val productQnt = item.units.firstOrNull()?.productQnt?.toIntOrNull() ?: 1
    val availableExQuantity = availableQuantity / productQnt
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Введите количество", 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ) 
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.name, 
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Доступное количество EX: $availableExQuantity \nОбщее кол-во: $availableQuantity\nВложенность ЕХ: $productQnt"
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            quantity = it
                            errorMessage = null
                            
                            // Проверяем количество при вводе
                            val quantityInt = it.toIntOrNull() ?: 0
                            if (quantityInt > availableExQuantity) {
                                errorMessage = "Максимум: $availableExQuantity EX"
                            }
                        }
                    },
                    label = { Text("Количество (EX)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colors.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull() ?: 0
                    if (quantityInt <= 0) {
                        errorMessage = "Число должно быть больше 0"
                    } else if (quantityInt > availableExQuantity) {
                        errorMessage = "Максимум: $availableExQuantity EX"
                    } else {
                        // Умножаем на productQnt для получения общего количества
                        onConfirm(quantityInt * productQnt)
                    }
                },
                enabled = quantity.isNotEmpty()
            ) {
                Text("ОК", fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", fontSize = 14.sp)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun CompactTargetLocationDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    scannerViewModel: ScannerViewModel
) {
    var locationId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Целевая ячейка", 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ) 
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = locationId,
                        onValueChange = { 
                            locationId = it
                            errorMessage = null
                        },
                        label = { Text("ID ячейки", fontSize = 12.sp) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))

                }
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colors.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Введите ID или отсканируйте штрих-код",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (locationId.isNotEmpty()) {
                        onConfirm(locationId)
                    } else {
                        errorMessage = "Введите ID ячейки"
                    }
                },
                enabled = locationId.isNotEmpty()
            ) {
                Text("ОК", fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", fontSize = 14.sp)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun CompactProductCard(
    item: LocationItem,
    onSelectClick: () -> Unit
) {
    val unit = item.units.firstOrNull()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Арт: ${item.article}",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "ШК: ${item.shk}",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (unit != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Количество: ${unit.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = unit.prunitName,
                        fontSize = 12.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (unit.expirationDate != null) {
                        Text(
                            text = "Годен до: ${unit.expirationDate}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text(
                        text = "Сост.: ${unit.conditionState}",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Button(
                onClick = onSelectClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ВЫБРАТЬ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Выбрать",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
