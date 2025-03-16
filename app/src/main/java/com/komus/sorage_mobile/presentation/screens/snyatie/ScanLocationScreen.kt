package com.komus.sorage_mobile.presentation.screens.snyatie

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.komus.sorage_mobile.domain.state.PickState
import com.komus.sorage_mobile.domain.viewModel.PickViewModel
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.delay

@Composable
fun ScanLocationScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel,
    pickViewModel: PickViewModel,
    spHelper: SPHelper
) {
    // Состояния
    var locationId by rememberSaveable { mutableStateOf("") }
    val skladId = spHelper.getSkladId()
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val locationItemsState by pickViewModel.locationItemsState.collectAsStateWithLifecycle()
    val pickState by pickViewModel.pickState.collectAsStateWithLifecycle()
    var scanMode by rememberSaveable { mutableStateOf(ScanMode.LOCATION) }
    var showScrollIndicator by remember { mutableStateOf(true) }
    
    // Состояния для диалога ввода количества
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<LocationItem?>(null) }
    var quantity by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    // Скрываем индикатор прокрутки после небольшой задержки
    LaunchedEffect(Unit) {
        delay(3000)
        showScrollIndicator = false
    }
    
    // Очищаем состояние при входе на экран
    LaunchedEffect(Unit) {
        Log.d("ScanLocationScreen", "Инициализация экрана")
        pickViewModel.resetLocationItemsState()
        pickViewModel.resetPickState()
        pickViewModel.setSkladId(skladId)
    }
    
    // Обрабатываем сканирование
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            val scannedCode = barcodeData
            scannerViewModel.clearBarcode()
            
            when (scanMode) {
                ScanMode.LOCATION -> {
                    Log.d("ScanLocationScreen", "Сканирована ячейка: $scannedCode")
                    locationId = scannedCode
                    pickViewModel.getLocationItems(locationId)
                }
                ScanMode.PRODUCT -> {
                    Log.d("ScanLocationScreen", "Сканирован товар: $scannedCode")
                    pickViewModel.searchProductByBarcode(scannedCode)
                }
            }
        }
    }
    
    // Обработка успешного снятия товара
    LaunchedEffect(pickState) {
        if (pickState is PickState.Success) {
            // Показываем сообщение об успешном снятии
            delay(1500)
            
            // Сбрасываем состояние экрана
            locationId = ""
            pickViewModel.resetLocationItemsState()
            pickViewModel.resetPickState()
            showQuantityDialog = false
            quantity = ""
            validationError = null
            selectedItem = null
            scanMode = ScanMode.LOCATION
        }
    }
    
    // Диалог ввода количества для снятия товара
    if (showQuantityDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { 
                showQuantityDialog = false 
                quantity = ""
                validationError = null
            },
            title = { 
                Text(
                    "Снятие товара", 
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(modifier = Modifier.padding(0.dp)) {
                    // Информация о товаре
                    Text(
                        text = selectedItem!!.name,
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "Артикул: ${selectedItem!!.article}",
                        style = MaterialTheme.typography.caption
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Получаем количество из первого элемента units, если он есть
                    val availableQuantity = if (selectedItem!!.units.isNotEmpty()) {
                        selectedItem!!.units[0].quantity
                    } else {
                        selectedItem!!.quantity?.toString() ?: "Н/Д"
                    }
                    
                    Text(
                        text = "Доступное количество: $availableQuantity",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Поле ввода количества
                    Text(
                        text = "Введите количество для снятия:",
                        style = MaterialTheme.typography.body2
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextField(
                        value = quantity,
                        onValueChange = { 
                            // Проверяем, что введено число
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                quantity = it
                                validationError = null
                            }
                        },
                        label = { Text("Количество") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Отображение ошибки валидации
                    validationError?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Ошибка",
                                tint = MaterialTheme.colors.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                    
                    // Отображение состояния снятия товара
                    when (pickState) {
                        is PickState.Loading -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Выполняется снятие товара...",
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PickState.Error -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            val errorMessage = (pickState as PickState.Error).message
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color(0xFFFFEBEE),
                                elevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Ошибка",
                                            tint = MaterialTheme.colors.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ошибка снятия товара",
                                            color = MaterialTheme.colors.error,
                                            style = MaterialTheme.typography.body2,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }
                        is PickState.Success -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color(0xFFE8F5E9),
                                elevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Успешно",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Товар успешно снят!",
                                        color = Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quantity.isNotEmpty()) {
                            val qnt = quantity.toInt()
                            
                            // Получаем доступное количество
                            val availableQuantity = if (selectedItem!!.units.isNotEmpty()) {
                                selectedItem!!.units[0].quantity.toIntOrNull() ?: 0
                            } else {
                                selectedItem!!.quantity ?: 0
                            }
                            
                            if (qnt <= 0) {
                                validationError = "Количество должно быть больше нуля"
                            } else if (qnt > availableQuantity) {
                                validationError = "Недостаточно товара в ячейке"
                            } else {
                                // Выполняем снятие товара
                                Log.d("ScanLocationScreen", "Снятие товара: ${selectedItem!!.id}, количество: $qnt")
                                pickViewModel.pickFromLocationBySkladId(
                                    qnt,
                                    spHelper.getUserName()
                                )
                            }
                        } else {
                            validationError = "Введите количество"
                        }
                    },
                    enabled = pickState !is PickState.Loading
                ) {
                    Text("ПОДТВЕРДИТЬ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showQuantityDialog = false 
                        quantity = ""
                        validationError = null
                    },
                    enabled = pickState !is PickState.Loading
                ) {
                    Text("ОТМЕНА")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Снятие товара") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        // Основной контейнер с прокруткой
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 0.dp) // Убираем отступ для нижней навигации
        ) {
            // Индикатор прокрутки
            if (showScrollIndicator) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↓",
                        color = MaterialTheme.colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp) // Уменьшаем горизонтальные отступы для маленького экрана
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp)) // Уменьшаем верхний отступ
                
                // Переключатель режима сканирования
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "Режим сканирования",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        
                        TabRow(
                            selectedTabIndex = scanMode.ordinal,
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.primary
                        ) {
                            Tab(
                                selected = scanMode == ScanMode.LOCATION,
                                onClick = { scanMode = ScanMode.LOCATION },
                                text = { Text("Сканировать ячейку") }
                            )
                            Tab(
                                selected = scanMode == ScanMode.PRODUCT,
                                onClick = { scanMode = ScanMode.PRODUCT },
                                text = { Text("Сканировать товар") }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp)) // Уменьшаем отступ между карточками
                
                // Содержимое в зависимости от режима сканирования
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp) // Уменьшаем внутренний отступ
                    ) {
                        when (scanMode) {
                            ScanMode.LOCATION -> {
                                Text(
                                    text = "Отсканируйте или введите ШК ячейки",
                                    style = MaterialTheme.typography.subtitle2, // Уменьшаем размер шрифта
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp)) // Уменьшаем отступ
                                
                                OutlinedTextField(
                                    value = locationId,
                                    onValueChange = { locationId = it.trim() },
                                    label = { Text("ШК ячейки") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Поиск"
                                        )
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = {
                                        if (locationId.isNotEmpty()) {
                                            pickViewModel.getLocationItems(locationId)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = locationId.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.primary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        "НАЙТИ ТОВАРЫ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            ScanMode.PRODUCT -> {
                                Text(
                                    text = "Отсканируйте штрихкод товара",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    backgroundColor = Color(0xFFE3F2FD),
                                    elevation = 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Информация",
                                            tint = Color(0xFF1976D2)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Для поиска товара отсканируйте его штрихкод",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp)) // Уменьшаем отступ между карточками
                
                // Отображение состояния загрузки или результатов
                when (locationItemsState) {
                    is LocationItemsState.Loading -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp), // Уменьшаем отступ
                            elevation = 2.dp // Уменьшаем тень
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp) // Уменьшаем отступ
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp)) // Уменьшаем размер индикатора
                                Spacer(modifier = Modifier.height(4.dp)) // Уменьшаем отступ
                                Text(
                                    text = "Загрузка товаров...",
                                    style = MaterialTheme.typography.caption // Уменьшаем размер шрифта
                                )
                            }
                        }
                    }
                    is LocationItemsState.Error -> {
                        val errorMessage = (locationItemsState as LocationItemsState.Error).message
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp), // Уменьшаем отступ
                            elevation = 2.dp, // Уменьшаем тень
                            backgroundColor = Color(0xFFFFEBEE)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp) // Уменьшаем отступ
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Ошибка",
                                        tint = MaterialTheme.colors.error,
                                        modifier = Modifier.size(16.dp) // Уменьшаем размер иконки
                                    )
                                    Spacer(modifier = Modifier.width(4.dp)) // Уменьшаем отступ
                                    Text(
                                        text = "Ошибка",
                                        style = MaterialTheme.typography.subtitle2, // Уменьшаем размер шрифта
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.error
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp)) // Уменьшаем отступ
                                
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.caption // Уменьшаем размер шрифта
                                )
                            }
                        }
                    }
                    is LocationItemsState.Success -> {
                        val items = (locationItemsState as LocationItemsState.Success).items
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp), // Уменьшаем отступ
                            elevation = 2.dp, // Уменьшаем тень
                            backgroundColor = Color(0xFFE8F5E9)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp) // Уменьшаем отступ
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Успешно",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp) // Уменьшаем размер иконки
                                    )
                                    Spacer(modifier = Modifier.width(4.dp)) // Уменьшаем отступ
                                    Text(
                                        text = "Найдено товаров: ${items.size}",
                                        style = MaterialTheme.typography.subtitle2, // Уменьшаем размер шрифта
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp)) // Уменьшаем отступ между карточками товаров
                        
                        if (items.isEmpty()) {
                            // Если товаров не найдено
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "В ячейке не найдено товаров",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Проверьте правильность ввода кода ячейки или отсканируйте ячейку повторно",
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        } else {
                            // Отображаем список товаров
                            Text(
                                text = "Товары в ячейке:",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Start
                            )
                            
                            items.forEach { item ->
                                ProductItemCard(
                                    item = item,
                                    onClick = {
                                        Log.d("ScanLocationScreen", "Выбран товар: ${item.name}")
                                        selectedItem = item
                                        pickViewModel.selectItem(item)
                                        showQuantityDialog = true
                                        quantity = ""
                                        validationError = null
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    else -> {}
                }
                
                // Добавляем минимальное пространство внизу для прокрутки
                Spacer(modifier = Modifier.height(8.dp)) // Минимальный отступ внизу
            }
        }
    }
}

@Composable
fun ProductItemCard(item: LocationItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(1.dp, RoundedCornerShape(4.dp)),
        elevation = 1.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Артикул: ${item.article}",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp
                )
                
                Text(
                    text = "Штрихкод: ${item.shk}",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (item.units.isNotEmpty()) {
                Column {
                    item.units.forEachIndexed { index, unit ->
                        if (index > 0) {
                            Divider(modifier = Modifier.padding(vertical = 2.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Тип: ${unit.prunitName}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
                            )
                            
                            Text(
                                text = "Кол-во: ${unit.quantity}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = "Состояние: ${unit.conditionState}",
                            style = MaterialTheme.typography.caption,
                            fontSize = 9.sp
                        )
                        
                        if (unit.expirationDate != null) {
                            Text(
                                text = "Срок годности: ${unit.expirationDate}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Доступное количество:",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp
                    )
                    
                    Text(
                        text = "${item.quantity ?: "Н/Д"}",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "СНЯТЬ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class ScanMode {
    LOCATION, PRODUCT
} 