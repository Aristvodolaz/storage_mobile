package com.komus.sorage_mobile.presentation.screens.inventory

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.model.ConditionReasons
import com.komus.sorage_mobile.domain.model.InventoryItem
import com.komus.sorage_mobile.domain.model.SearchType
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import com.komus.sorage_mobile.domain.viewModel.InventoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    scannerViewModel: ScannerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()


    // Отображение сообщения об ошибке в Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            val result = scaffoldState.snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "ОК"
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.resetError()
            }
        }
    }

    LaunchedEffect(barcodeData){
        if (barcodeData.isNotEmpty()) {
            viewModel.setSearchQuery(barcodeData)
        }
        scannerViewModel.clearBarcode()
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Инвентаризация", fontSize = 16.sp) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White,
                    elevation = 4.dp,
                    actions = {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else if (uiState.hasUnsyncedChanges) {
                            IconButton(onClick = { viewModel.requestSync() }) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Синхронизировать",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
                if (uiState.hasUnsyncedChanges) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Есть несинхронизированные изменения",
                            fontSize = 12.sp,
                            color = Color(0xFFF57C00)
                        )
                    }
                }
            }
        },
        snackbarHost = { hostState ->
            SnackbarHost(hostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        data.actionLabel?.let { actionLabel ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(
                                    text = actionLabel,
                                    color = Color.White
                                )
                            }
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Информация",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = data.message,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            // Tabs для переключения между типами поиска
            TabRow(
                selectedTabIndex = if (uiState.searchType == SearchType.LOCATION_ID) 0 else 1,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary
            ) {
                Tab(
                    selected = uiState.searchType == SearchType.LOCATION_ID,
                    onClick = { viewModel.setSearchType(SearchType.LOCATION_ID) },
                    text = { 
                        Text(
                            "По ячейке",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    }
                )
                Tab(
                    selected = uiState.searchType == SearchType.PRODUCT_ARTICLE,
                    onClick = { viewModel.setSearchType(SearchType.PRODUCT_ARTICLE) },
                    text = { 
                        Text(
                            "По артикулу",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    }
                )
            }
            
            // Поисковая строка с кнопкой
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = if (uiState.searchType == SearchType.LOCATION_ID) 
                                "Введите ID ячейки" 
                            else 
                                "Введите артикул товара",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (uiState.searchType == SearchType.LOCATION_ID) 
                            KeyboardType.Text 
                        else 
                            KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            viewModel.search()
                        }
                    ),
                    textStyle = MaterialTheme.typography.body1.copy(fontSize = 14.sp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))


                
                Spacer(modifier = Modifier.width(4.dp))
                
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.search()
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Поиск",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Найти", fontSize = 12.sp)
                }
            }
            
            // Список результатов
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных для отображения",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(uiState.items) { item ->
                        InventoryItemCard(item = item, onClick = { viewModel.selectItem(item) })
                    }
                }
            }
        }
    }
    
    // Диалог с деталями товара
    if (uiState.showDetailsDialog && uiState.selectedItem != null) {
        ItemDetailsDialog(
            item = uiState.selectedItem!!,
            onConfirm = { viewModel.confirmItem() },
            onEdit = { viewModel.showUpdateDialog() },
            onDismiss = { viewModel.hideDialogs() }
        )
    }
    
    // Диалог изменения количества
    if (uiState.showUpdateDialog && uiState.selectedItem != null) {
        UpdateQuantityDialog(
            item = uiState.selectedItem!!,
            onUpdate = { newQuantity, newExpirationDate, newCondition, newReason -> 
                viewModel.updateItem(newQuantity, newExpirationDate, newCondition, newReason)
            },
            onDismiss = { viewModel.hideDialogs() }
        )
    }
}

@Composable
fun InventoryItemCard(item: InventoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = 2.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Статус проверки
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (item.isChecked) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Проверено",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Информация о товаре
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row {
                    Text(
                        text = "Артикул: ${item.article}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Ячейка: ${item.locationId}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Количество
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${item.actualQuantity} шт.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (item.actualQuantity != item.expectedQuantity) 
                        MaterialTheme.colors.error 
                    else 
                        MaterialTheme.colors.onSurface
                )

            }
        }
    }
}

@Composable
fun ItemDetailsDialog(
    item: InventoryItem,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Информация о товаре", fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow(
                    label = "Артикул:",
                    value = item.article
                )
                
                InfoRow(
                    label = "Штрихкод:",
                    value = item.barcode
                )
                
                InfoRow(
                    label = "Ячейка:",
                    value = item.locationName.ifEmpty { item.locationId }
                )

                
                InfoRow(
                    label = "Фактическое кол-во:",
                    value = "${item.actualQuantity} шт.",
                )

                InfoRow(
                    label = "Срок годности:",
                    value = item.expirationDate.ifEmpty { "Не указан" }
                )

                InfoRow(
                    label = "Состояние:",
                    value = item.condition,
                    valueColor = if (item.condition == "Некондиция") 
                        MaterialTheme.colors.error else MaterialTheme.colors.onSurface
                )

                if (item.condition == "Некондиция" && !item.reason.isNullOrEmpty()) {
                    InfoRow(
                        label = "Причина некондиции:",
                        value = item.reason,
                        valueColor = MaterialTheme.colors.error
                    )
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Изменить",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Изменить", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(onClick = onConfirm) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Подтвердить",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Подтвердить", fontSize = 12.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colors.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.4f)
        )
        
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpdateQuantityDialog(
    item: InventoryItem,
    onUpdate: (Int, String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(item.actualQuantity.toString()) }
    var startDate by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var expirationDate by remember { 
        mutableStateOf(
            try {
                if (item.expirationDate.isNotEmpty()) {
                    val date = LocalDate.parse(item.expirationDate, DateTimeFormatter.ofPattern("MMM dd yyyy hh:mma", Locale.ENGLISH))
                    date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                } else {
                    item.expirationDate
                }
            } catch (e: Exception) {
                try {
                    if (item.expirationDate.isNotEmpty()) {
                        val date = LocalDate.parse(item.expirationDate, DateTimeFormatter.ISO_DATE)
                        date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    } else {
                        item.expirationDate
                    }
                } catch (e2: Exception) {
                    item.expirationDate
                }
            }
        )
    }
    var condition by remember { mutableStateOf(item.condition) }
    var reason by remember { mutableStateOf(item.reason ?: "") }
    var showReasonError by remember { mutableStateOf(false) }
    var showExpirationAlert by remember { mutableStateOf(false) }
    var showExpirationRequiredAlert by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var skipExpirationDate by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Функция для расчета даты окончания срока годности
    fun calculateExpirationDate() {
        if (skipExpirationDate) return
        
        try {
            if (startDate.isNotEmpty()) {
                val date = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val daysToAdd = days.toIntOrNull() ?: 0
                val monthsToAdd = months.toIntOrNull() ?: 0
                
                val calculatedDate = date
                    .plusMonths(monthsToAdd.toLong())
                    .plusDays(daysToAdd.toLong())
                
                expirationDate = calculatedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                
                // Проверяем, не истек ли срок годности
                val isoDate = calculatedDate.format(DateTimeFormatter.ISO_DATE)
                if (ExpirationDateValidator.isExpired(isoDate) && condition == "Кондиция") {
                    showExpirationAlert = true
                    }
                }
        } catch (e: Exception) {
            // Игнорируем ошибки парсинга при вводе
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Заголовок
                Text(
                    text = "Изменить данные товара",
                    style = MaterialTheme.typography.h6,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Название товара
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Артикул: ${item.article}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Введите фактическое количество:",
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!skipExpirationDate) {
                    Text(
                        text = "Дата начала срока годности (дд.мм.гггг):",
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { 
                                startDate = it
                                calculateExpirationDate()
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                // Устанавливаем дату 01.01.2999 и очищаем поля
                                startDate = ""
                                days = ""
                                months = ""
                                expirationDate = "01.01.2999"
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
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Месяцев:",
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                                value = months,
                        onValueChange = { 
                                    months = it
                                    calculateExpirationDate()
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Дней:",
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            OutlinedTextField(
                                value = days,
                                onValueChange = { 
                                    days = it
                                    calculateExpirationDate()
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Итоговый срок годности:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (expirationDate.isNotEmpty()) expirationDate else "Не рассчитан",
                        fontSize = 14.sp,
                        color = if (expirationDate.isNotEmpty()) MaterialTheme.colors.primary else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "Срок годности: 01.01.2999 (пропущен)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            skipExpirationDate = false
                            expirationDate = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Ввести дату вручную", fontSize = 10.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                                if (expirationDate.isEmpty()) {
                                    showExpirationRequiredAlert = true
                                    return@RadioButton
                                }
                                
                                    try {
                                        val date = LocalDate.parse(expirationDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                        val isoDate = date.format(DateTimeFormatter.ISO_DATE)
                                        if (ExpirationDateValidator.isExpired(isoDate)) {
                                            showExpirationAlert = true
                                            return@RadioButton
                                        }
                                    } catch (e: Exception) {
                                    showExpirationRequiredAlert = true
                                    return@RadioButton
                                    }
                                
                                condition = "Кондиция"
                                showReasonError = false
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Кондиция",
                            fontSize = 12.sp
                        )
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
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Некондиция",
                            fontSize = 12.sp
                        )
                    }
                }

                if (condition == "Некондиция") {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Причина некондиции:",
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                                    fontSize = 10.sp
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
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (reason == reasonOption) 
                                                MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                            else 
                                                MaterialTheme.colors.surface
                                        )
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

                // Кнопки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                        .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                    Text("Отмена", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        if (condition == "Некондиция" && reason.isEmpty()) {
                            showReasonError = true
                            return@Button
                        }
                        
                        if (!skipExpirationDate && condition == "Кондиция") {
                            if (expirationDate.isEmpty()) {
                                showExpirationRequiredAlert = true
                                return@Button
                            }
                            
                            try {
                                val date = LocalDate.parse(expirationDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                val isoDate = date.format(DateTimeFormatter.ISO_DATE)
                                
                                if (ExpirationDateValidator.isExpired(isoDate)) {
                                    showExpirationAlert = true
                                    return@Button
                                }
                            } catch (e: Exception) {
                                showExpirationRequiredAlert = true
                                return@Button
                            }
                        }
                        
                        quantity.toIntOrNull()?.let { 
                            val finalExpirationDate = if (skipExpirationDate) "01.01.2999" else expirationDate
                            onUpdate(
                                it,
                                finalExpirationDate,
                                condition,
                                if (condition == "Некондиция") reason else null
                            )
                            keyboardController?.hide()
                        }
                    }
                ) {
                    Text("Сохранить", fontSize = 12.sp)
                }
            }
        }
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
                        condition = "Некондиция"
                    }
                ) {
                    Text("Установить 'Некондиция'")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpirationAlert = false }) {
                    Text("Отмена")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }
    
    // Диалог предупреждения об обязательном вводе срока годности
    if (showExpirationRequiredAlert) {
        AlertDialog(
            onDismissRequest = { showExpirationRequiredAlert = false },
            title = { Text("Внимание!") },
            text = { 
                Column {
                    Text("Для состояния 'Кондиция' необходимо указать срок годности.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Пожалуйста, заполните поле срока годности.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showExpirationRequiredAlert = false
                    }
                ) {
                    Text("ОК")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }
} 