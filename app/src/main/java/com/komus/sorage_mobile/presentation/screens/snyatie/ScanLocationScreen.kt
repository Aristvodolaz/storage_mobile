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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle

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
    pickViewModel: PickViewModel = hiltViewModel(),
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

                ScanMode.PRODUCT -> TODO()
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
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ) 
            },
            text = {
                Column(modifier = Modifier.padding(0.dp)) {
                    // Информация о товаре
                    Text(
                        text = selectedItem!!.name,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "Артикул: ${selectedItem!!.article}",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Получаем количество из первого элемента units, если он есть
                    val availableQuantity = if (selectedItem!!.units.isNotEmpty()) {
                        selectedItem!!.units[0].quantity
                    } else {
                        selectedItem!!.units[0].quantity?.toString() ?: "Н/Д"
                    }

                    val productQnt = selectedItem?.units?.getOrNull(0)?.productQnt?.toDouble() ?: 1.0
                    val justCoun = availableQuantity.toDouble() / productQnt


                    Text(
                        text = "Доступное количество: $justCoun",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Количество в еденицах: $availableQuantity",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        fontSize = 10.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Поле ввода количества
                    Text(
                        text = "Введите количество для снятия:",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    TextField(
                        value = quantity,
                        onValueChange = { 
                            // Проверяем, что введено число
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                quantity = it
                                validationError = null
                            }
                        },
                        label = { Text("Количество", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
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
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
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
                                selectedItem!!.units[0].quantity ?: 0
                            }
                            
                            if (qnt <= 0) {
                                validationError = "Количество должно быть больше нуля"
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
                    enabled = pickState !is PickState.Loading,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ПОДТВЕРДИТЬ", fontSize = 10.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showQuantityDialog = false 
                        quantity = ""
                        validationError = null
                    },
                    enabled = pickState !is PickState.Loading,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ОТМЕНА", fontSize = 10.sp)
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Снятие товара",
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                modifier = Modifier.height(44.dp)
            )
        }
    ) { paddingValues ->
        // Основной контейнер с прокруткой
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 0.dp)
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
                    .padding(horizontal = 8.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                
                // Переключатель режима сканирования
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "Режим сканирования",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        
                        TabRow(
                            selectedTabIndex = scanMode.ordinal,
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.primary,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Tab(
                                selected = scanMode == ScanMode.LOCATION,
                                onClick = { scanMode = ScanMode.LOCATION },
                                text = { 
                                    Text(
                                        "Сканировать ячейку",
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Содержимое в зависимости от режима сканирования
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp)
                    ) {
                        when (scanMode) {
                            ScanMode.LOCATION -> {
                                Text(
                                    text = "Отсканируйте или введите ШК ячейки",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = locationId,
                                        onValueChange = { locationId = it.trim() },
                                        label = { 
                                            Text(
                                                "ШК ячейки",
                                                fontSize = 10.sp
                                            ) 
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Поиск",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    // Добавляем кнопку сканирования для TСД
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Button(
                                    onClick = {
                                        if (locationId.isNotEmpty()) {
                                            pickViewModel.getLocationItems(locationId)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    enabled = locationId.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.primary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        "НАЙТИ ТОВАРЫ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Отображение состояния загрузки или результатов
                when (locationItemsState) {
                    is LocationItemsState.Loading -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            elevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Загрузка товаров...",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    is LocationItemsState.Error -> {
                        val errorMessage = (locationItemsState as LocationItemsState.Error).message
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            elevation = 1.dp,
                            backgroundColor = Color(0xFFFFEBEE)
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
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ошибка",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.error,
                                        fontSize = 10.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    is LocationItemsState.Success -> {
                        val items = (locationItemsState as LocationItemsState.Success).items
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            elevation = 1.dp,
                            backgroundColor = Color(0xFFE8F5E9)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Успешно",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Найдено товаров: ${items.size}",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        if (items.isEmpty()) {
                            // Если товаров не найдено
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 2.dp,
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "В ячейке не найдено товаров",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "Проверьте правильность ввода кода ячейки или отсканируйте ячейку повторно",
                                        style = MaterialTheme.typography.caption,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        } else {
                            // Отображаем список товаров
                            Text(
                                text = "Товары в ячейке:",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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
                                
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    else -> {}
                }
                
                // Добавляем минимальное пространство внизу для прокрутки
                Spacer(modifier = Modifier.height(4.dp))
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
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Арт: ${item.article}",
                    style = MaterialTheme.typography.caption,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "ШК: ${item.shk}",
                    style = MaterialTheme.typography.caption,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Divider(thickness = 0.5.dp)
            
            Spacer(modifier = Modifier.height(2.dp))
            
            if (item.units.isNotEmpty()) {
                Column {
                    item.units.forEachIndexed { index, unit ->
                        if (index > 0) {
                            Divider(modifier = Modifier.padding(vertical = 1.dp), thickness = 0.5.dp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Тип: ${unit.prunitName}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = "Кол-во: ${unit.quantity}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(1.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Состояние: ${unit.conditionState}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 7.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (unit.expirationDate != null) {
                                Text(
                                    text = "Годен до: ${unit.expirationDate}",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 7.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
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
                        fontSize = 8.sp
                    )
                    
                    Text(
                        text = "${item.units[0].quantity ?: "Н/Д"}",
                        style = MaterialTheme.typography.caption,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "СНЯТЬ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ScanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.primary)
            .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Сканировать",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

enum class ScanMode {
    LOCATION, PRODUCT
} 