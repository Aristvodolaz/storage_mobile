package com.komus.sorage_mobile.presentation.screens.snyatie

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.state.LocationItemsState
import com.komus.sorage_mobile.domain.state.MoveProductState
import com.komus.sorage_mobile.domain.viewModel.PickViewModel
import com.komus.sorage_mobile.util.ScanMode

// Временная заглушка для ScannerView, пока не найдем оригинальный компонент
@Composable
fun ScannerView(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Сканер штрих-кодов",
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MoveProductScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel,
    pickViewModel: PickViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var sourceLocationId by remember { mutableStateOf("") }
    var targetLocationId by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var conditionState by remember { mutableStateOf("кондиция") }
    var executor by remember { mutableStateOf("") }
    
    val locationItemsState by pickViewModel.locationItemsState.collectAsState()
    val moveProductState by pickViewModel.moveProductState.collectAsState()
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()

    var scanMode by remember { mutableStateOf(ScanMode.SOURCE_LOCATION) }
    var selectedItem by remember { mutableStateOf<LocationItem?>(null) }
    var showQuantityInput by remember { mutableStateOf(false) }
    
    // Получаем имя пользователя из SharedPreferences
    LaunchedEffect(Unit) {
        executor = scannerViewModel.getUserName()
    }
    
    // Обработка сканирования
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            when (scanMode) {
                ScanMode.SOURCE_LOCATION -> {
                    sourceLocationId = barcodeData
                    pickViewModel.setSourceLocation(barcodeData)
                    pickViewModel.getLocationItems(barcodeData)
                }
                ScanMode.TARGET_LOCATION -> {
                    targetLocationId = barcodeData
                    pickViewModel.setTargetLocation(barcodeData)
                    
                    // Автоматически выполняем перемещение после сканирования целевой ячейки
                    if (selectedItem != null && quantity.isNotEmpty()) {
                        selectedItem!!.units[0].expirationDate?.let {
                            pickViewModel.moveProduct(
                                quantity = quantity.toInt(),
                                conditionState = selectedItem!!.units[0].conditionState,
                                expirationDate = it,
                                executor = executor
                            )
                        } ?: run {
                            pickViewModel.moveProduct(
                                quantity = quantity.toInt(),
                                conditionState = selectedItem!!.units[0].conditionState,
                                expirationDate = "2025-01-01",
                                executor = executor
                            )
                        }
                    }
                }
                else -> {
                    // Обработка других режимов сканирования, если они будут добавлены
                }
            }
            
            scannerViewModel.clearBarcode()
        }
    }
    
    // Сброс состояния при успешном перемещении
    LaunchedEffect(moveProductState) {
        if (moveProductState is MoveProductState.Success) {
            // Очищаем поля после успешного перемещения
            sourceLocationId = ""
            targetLocationId = ""
            quantity = "1"
            selectedItem = null
            showQuantityInput = false
            scanMode = ScanMode.SOURCE_LOCATION
            pickViewModel.resetMoveProductState()
            pickViewModel.resetLocationItemsState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Перемещение товара") },
                navigationIcon = {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Индикатор прокрутки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Прокрутите для просмотра",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Секция сканирования
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = when {
                                showQuantityInput && scanMode == ScanMode.TARGET_LOCATION -> "Отсканируйте целевую ячейку"
                                scanMode == ScanMode.SOURCE_LOCATION -> "Отсканируйте исходную ячейку"
                                else -> "Отсканируйте исходную ячейку"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ScannerView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colors.primary,
                                    RoundedCornerShape(8.dp)
                                ),
                            onBarcodeScanned = { barcode ->
                                scannerViewModel.setScannedValue(barcode)
                            }
                        )
                    }
                }
                
                // Секция исходной ячейки
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 4.dp,
                    backgroundColor = if (sourceLocationId.isNotEmpty()) Color(0xFFE8F5E9) else MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Исходная ячейка",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = sourceLocationId,
                            onValueChange = { 
                                sourceLocationId = it
                                pickViewModel.setSourceLocation(it)
                                if (it.isNotEmpty()) {
                                    pickViewModel.getLocationItems(it)
                                }
                            },
                            label = { Text("ID исходной ячейки") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                            )
                        )
                        
                        if (sourceLocationId.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Ячейка выбрана",
                                    tint = Color.Green,
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.padding(4.dp))
                                
                                Text(
                                    text = "Исходная ячейка выбрана",
                                    color = Color.Green
                                )
                            }
                        }
                    }
                }
                
                // Секция товара
                if (!showQuantityInput) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Выберите товар для перемещения",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            when (locationItemsState) {
                                is LocationItemsState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is LocationItemsState.Success -> {
                                    val items = (locationItemsState as LocationItemsState.Success).items
                                    if (items.isNotEmpty()) {
                                        Column {
                                            Text(
                                                text = "Найдено товаров: ${items.size}",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.primary
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            items.forEach { item ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .clickable {
                                                            selectedItem = item
                                                            pickViewModel.selectItem(item)
                                                            showQuantityInput = true
                                                        },
                                                    elevation = 2.dp,
                                                    backgroundColor = if (selectedItem?.id == item.id) Color(0xFFE3F2FD) else MaterialTheme.colors.surface
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp)
                                                    ) {
                                                        Text(
                                                            text = item.name ?: "Неизвестный товар",
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        
                                                        Text(text = "Артикул: ${item.article}")
                                                        Text(text = "Штрихкод: ${item.shk}")
                                                        Text(text = "Доступное количество: ${item.units[0].quantity}")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                is LocationItemsState.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Ошибка",
                                                tint = Color.Red,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Text(
                                                text = (locationItemsState as LocationItemsState.Error).message,
                                                color = Color.Red,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Отсканируйте исходную ячейку для просмотра товаров",
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Секция ввода количества и целевой ячейки
                if (showQuantityInput) {
                    // Информация о выбранном товаре
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp,
                        backgroundColor = Color(0xFFE3F2FD)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Выбранный товар",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            selectedItem?.let { item ->
                                Text(
                                    text = item.name ?: "Неизвестный товар",
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(text = "Артикул: ${item.article}")
                                Text(text = "Штрихкод: ${item.shk}")
                                Text(text = "Доступное количество: ${item.units[0].quantity}")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { 
                                    showQuantityInput = false
                                    selectedItem = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.Gray,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Выбрать другой товар")
                            }
                        }
                    }
                    
                    // Секция ввода количества
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Количество для перемещения",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { 
                                    val newValue = it.filter { char -> char.isDigit() }
                                    quantity = newValue
                                },
                                label = { Text("Количество") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = conditionState,
                                onValueChange = { conditionState = it },
                                label = { Text("Состояние товара") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            var expirationDate by remember { mutableStateOf("2025-01-14") }
                            
                            OutlinedTextField(
                                value = expirationDate,
                                onValueChange = { expirationDate = it },
                                label = { Text("Срок годности (ГГГГ-ММ-ДД)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = executor,
                                onValueChange = { executor = it },
                                label = { Text("Исполнитель") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                    
                    // Секция целевой ячейки
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp,
                        backgroundColor = if (targetLocationId.isNotEmpty()) Color(0xFFE8F5E9) else MaterialTheme.colors.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Целевая ячейка",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = targetLocationId,
                                onValueChange = { 
                                    targetLocationId = it
                                    pickViewModel.setTargetLocation(it)
                                },
                                label = { Text("ID целевой ячейки") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                            )
                            
                            if (targetLocationId.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Ячейка выбрана",
                                        tint = Color.Green,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    
                                    Text(
                                        text = "Целевая ячейка выбрана",
                                        color = Color.Green
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Кнопка для перехода к сканированию целевой ячейки
                            Button(
                                onClick = {
                                    scanMode = ScanMode.TARGET_LOCATION
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onPrimary
                                ),
                                enabled = quantity.isNotEmpty() && quantity.toIntOrNull() != null && quantity.toInt() > 0
                            ) {
                                Text(
                                    text = "ОТСКАНИРОВАТЬ ЦЕЛЕВУЮ ЯЧЕЙКУ",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Секция статуса перемещения
                when (moveProductState) {
                    is MoveProductState.Loading -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Выполняется перемещение товара...",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    is MoveProductState.Success -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = 4.dp,
                            backgroundColor = Color(0xFFE8F5E9)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Успех",
                                        tint = Color.Green,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Товар успешно перемещен",
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = {
                                            sourceLocationId = ""
                                            targetLocationId = ""
                                            quantity = "1"
                                            selectedItem = null
                                            showQuantityInput = false
                                            scanMode = ScanMode.SOURCE_LOCATION
                                            pickViewModel.resetMoveProductState()
                                            pickViewModel.resetLocationItemsState()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.primary,
                                            contentColor = MaterialTheme.colors.onPrimary
                                        )
                                    ) {
                                        Text("Начать новое перемещение")
                                    }
                                }
                            }
                        }
                    }
                    is MoveProductState.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = 4.dp,
                            backgroundColor = Color(0xFFFFEBEE)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Ошибка",
                                        tint = Color.Red,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Ошибка перемещения товара",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = (moveProductState as MoveProductState.Error).message,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
                
                // Дополнительное пространство для прокрутки
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
} 