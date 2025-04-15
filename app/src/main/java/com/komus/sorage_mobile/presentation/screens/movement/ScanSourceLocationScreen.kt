package com.komus.sorage_mobile.presentation.screens.movement

import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.viewModel.MovementViewModel
import com.komus.sorage_mobile.presentation.components.LoadingIndicator
import com.komus.sorage_mobile.presentation.components.ScanButton
import com.komus.sorage_mobile.presentation.theme.Primary
import com.komus.sorage_mobile.util.SPHelper

private const val TAG = "ScanSourceLocationScreen"
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

    var selectedItem by remember { mutableStateOf<LocationItem?>(null) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showExpirationAndConditionDialog by remember { mutableStateOf(false) }
    var showTargetLocationDialog by remember { mutableStateOf(false) }

    var moveQuantity by remember { mutableStateOf(0) }
    var targetLocation by remember { mutableStateOf("") }

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
}

@Composable
fun ExpirationAndConditionDialog(
    onConfirm: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var expirationDate by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Кондиция") }
    var reason by remember { mutableStateOf("") }
    val conditionOptions = listOf("Кондиция", "Некондиция")

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
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = { expirationDate = it },
                    label = { Text("Срок годности (ДД.ММ.ГГГГ)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                conditionOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { condition = option }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = condition == option,
                            onClick = { condition = option }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option, fontSize = 14.sp)
                    }
                }

                if (condition == "Некондиция") {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Причина некондиции", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(expirationDate, condition, if (condition == "Некондиция") reason else null)
                },
                enabled = expirationDate.isNotEmpty() && (condition != "Некондиция" || reason.isNotEmpty())
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
                    text = "Доступно: $availableQuantity", 
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            quantity = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Количество", fontSize = 12.sp) },
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
                    } else if (quantityInt > availableQuantity) {
                        errorMessage = "Максимум: $availableQuantity"
                    } else {
                        onConfirm(quantityInt)
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
