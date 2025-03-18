package com.komus.sorage_mobile.presentation.screens.inventory

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.domain.model.InventoryItem
import com.komus.sorage_mobile.domain.model.SearchType
import com.komus.sorage_mobile.domain.viewModel.InventoryViewModel

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
            TopAppBar(
                title = { Text("Инвентаризация", fontSize = 16.sp) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 4.dp
            )
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
            onUpdate = { newQuantity -> viewModel.updateItemQuantity(newQuantity) },
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
                
                if (item.actualQuantity != item.expectedQuantity) {
                    Text(
                        text = "Ожидалось: ${item.expectedQuantity}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colors.error
                    )
                }
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
                InfoRow("Наименование:", item.name)
                InfoRow("Артикул:", item.article)
                InfoRow("Штрих-код:", item.barcode)
                InfoRow("ID ячейки:", item.locationId)
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                InfoRow(
                    label = "Количество:", 
                    value = "${item.actualQuantity} шт.",
                    valueColor = if (item.actualQuantity != item.expectedQuantity) 
                        MaterialTheme.colors.error 
                    else 
                        MaterialTheme.colors.onSurface
                )
                
                if (item.actualQuantity != item.expectedQuantity) {
                    InfoRow(
                        label = "Ожидаемое количество:",
                        value = "${item.expectedQuantity} шт.",
                        valueColor = MaterialTheme.colors.error
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "Статус: ${if (item.isChecked) "Проверено" else "Не проверено"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isChecked) Color(0xFF4CAF50) else Color.Gray
                )
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
fun UpdateQuantityDialog(
    item: InventoryItem,
    onUpdate: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(item.actualQuantity.toString()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить количество", fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    onValueChange = { 
                        // Разрешаем только цифры
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            quantity = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            quantity.toIntOrNull()?.let { onUpdate(it) }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.body1.copy(fontSize = 14.sp)
                )
                
                if (item.expectedQuantity != quantity.toIntOrNull()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ожидаемое количество: ${item.expectedQuantity} шт.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.error
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
                TextButton(onClick = onDismiss) {
                    Text("Отмена", fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        quantity.toIntOrNull()?.let { 
                            onUpdate(it)
                            keyboardController?.hide()
                        }
                    }
                ) {
                    Text("Сохранить", fontSize = 12.sp)
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