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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

/** Тип ЕХ паллета для учёта занимаемых адресов при сканировании ячейки */
enum class ExPalletType(
    val displayName: String,
    val addressesCount: Int,
    val description: String
) {
    EURO("Евро", 1, "1 адрес – выбранный"),
    FIN("FIN", 1, "1 адрес – выбранный"),
    EURO_X2("Евро*2", 2, "2 адреса – выбранный + рядом"),
    FIN_X2("FIN*2", 3, "3 адреса – выбранный + 2 рядом")
}

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
    var selectedExPalletType by remember { mutableStateOf<ExPalletType?>(null) }

    // Новое состояние для успешного диалога
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    val locationFocusRequester = remember { FocusRequester() }
    
    // Состояние для предупреждения о занятом адресе
    var showAddressWarningDialog by remember { mutableStateOf(false) }
    var addressWarningMessage by remember { mutableStateOf("") }

    // Обновление данных при возврате на экран
    LaunchedEffect(Unit) {
        if (locationId.isNotEmpty()) {
            movementViewModel.getLocationItems(locationId)
        }
        }
    
    // Автофокус на поле ячейки при загрузке экрана
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Небольшая задержка для инициализации UI
        locationFocusRequester.requestFocus()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(locationFocusRequester),
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
            onConfirm = { quantity, exPalletType ->
                moveQuantity = quantity
                selectedExPalletType = exPalletType
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
                
                // Проверяем, является ли перемещаемый товар ЕХ паллетом
                val isExPallet = selectedItem?.units?.any { unit ->
                    unit.prunitName.contains("ЕХ", ignoreCase = true) || 
                    unit.prunitName.contains("EX", ignoreCase = true)
                } ?: false
                
                if (isExPallet) {
                    // Показываем предупреждение для ЕХ паллет
                    addressWarningMessage = "Внимание! Вы перемещаете ЕХ паллет в ячейку $location. Убедитесь, что в ячейке не более 2-х ЕХ паллет."
                    showAddressWarningDialog = true
                }
                
                // Выполняем перемещение в любом случае
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
    
    // Диалог предупреждения о занятом адресе
    if (showAddressWarningDialog) {
        AlertDialog(
            onDismissRequest = { showAddressWarningDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Предупреждение",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Предупреждение",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            },
            text = {
                Text(
                    text = addressWarningMessage,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAddressWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    )
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
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при расчете даты: ${e.message}")
        }
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
                                
                                // Автоматически вызываем onConfirm с пропущенным сроком годности
                                onConfirm("2999-01-01", "Кондиция", null)
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
                    text = "Состояние товара: Кондиция",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
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
                        
                        // Автоматически используем состояние "Кондиция"
                        onConfirm(isoDate, "Кондиция", null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при сохранении даты: ${e.message}")
                    }
                },
                enabled = skipExpirationDate || startDate.isNotEmpty() || finalDate.isNotEmpty()
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
    onConfirm: (Int, ExPalletType?) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    val availableQuantity = item.units.firstOrNull()?.quantity?.toIntOrNull() ?: 0
    val productQnt = item.units.firstOrNull()?.productQnt?.toIntOrNull() ?: 1
    val availableExQuantity = availableQuantity / productQnt
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val quantityFocusRequester = remember { FocusRequester() }
    
    val isExPallet = item.units.any { unit ->
        unit.prunitName.contains("ЕХ", ignoreCase = true) || unit.prunitName.contains("EX", ignoreCase = true)
    }
    var selectedExPalletType by remember { mutableStateOf(ExPalletType.EURO) }
    
    // Автофокус на поле количества при открытии диалога
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Небольшая задержка для инициализации UI
        quantityFocusRequester.requestFocus()
    }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = item.name, 
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Доступное количество EX: $availableExQuantity \nОбщее кол-во: $availableQuantity\nВложенность ЕХ: $productQnt",
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            quantity = it
                            errorMessage = null
                            
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(quantityFocusRequester)
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colors.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
                
                if (isExPallet) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Тип ЕХ паллет (для учёта пустых ячеек)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExPalletType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { selectedExPalletType = type }
                                .background(
                                    if (selectedExPalletType == type) Primary.copy(alpha = 0.15f)
                                    else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedExPalletType == type,
                                onClick = { selectedExPalletType = type },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Primary,
                                    unselectedColor = Color.DarkGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = type.description,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
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
                        val exType = if (isExPallet) selectedExPalletType else null
                        onConfirm(quantityInt * productQnt, exType)
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
    val targetLocationFocusRequester = remember { FocusRequester() }
    
    // Автофокус на поле целевой ячейки при открытии диалога
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Небольшая задержка для инициализации UI
        targetLocationFocusRequester.requestFocus()
    }

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
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(targetLocationFocusRequester)
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
