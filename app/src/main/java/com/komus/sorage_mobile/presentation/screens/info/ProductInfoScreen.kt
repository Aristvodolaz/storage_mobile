package com.komus.sorage_mobile.presentation.screens.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.response.LocationProduct
import com.komus.sorage_mobile.data.response.ProductInfo
import com.komus.sorage_mobile.data.response.ProductLocation
import com.komus.sorage_mobile.domain.viewModel.ProductInfoViewModel
import com.komus.sorage_mobile.presentation.components.LoadingIndicator
import com.komus.sorage_mobile.presentation.components.ScanButton
import com.komus.sorage_mobile.presentation.theme.Primary
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProductInfoScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel,
    viewModel: ProductInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    
    // Обработка результата сканирования
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            viewModel.handleEvent(ProductInfoViewModel.UiEvent.OnSearchQueryChanged(barcodeData))
            viewModel.handleEvent(ProductInfoViewModel.UiEvent.OnSearchClicked)
            scannerViewModel.clearBarcode()
            focusManager.clearFocus()
        }
    }
    
    // Обработка эффектов
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ProductInfoViewModel.UiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
                null -> {}
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Поиск товара", 
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "Назад", 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                backgroundColor = Primary,
                elevation = 4.dp,
                // Уменьшаем высоту TopAppBar для маленького экрана
                modifier = Modifier.height(48.dp)
            )
        },
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(8.dp),
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier.padding(bottom = 8.dp),
                        content = {
                            Text(
                                text = data.message,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        action = data.actionLabel?.let {
                            {
                                TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                                    Text(it, fontSize = 12.sp)
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Компактные вкладки для выбора типа поиска
            ScrollableTabRow(
                selectedTabIndex = uiState.searchType.ordinal,
                backgroundColor = Primary,
                contentColor = Color.White,
                edgePadding = 4.dp,
                // Уменьшаем высоту вкладок
                modifier = Modifier.height(36.dp)
            ) {
                ProductInfoViewModel.SearchType.values().forEachIndexed { index, type ->
                    Tab(
                        selected = index == uiState.searchType.ordinal,
                        onClick = {
                            viewModel.handleEvent(
                                ProductInfoViewModel.UiEvent.OnSearchTypeChanged(type)
                            )
                        },
                        text = {
                            Text(
                                text = when (type) {
                                    ProductInfoViewModel.SearchType.LOCATION_ID -> "ШК ячейки"
                                    ProductInfoViewModel.SearchType.ARTICLE -> "Артикул"
                                    ProductInfoViewModel.SearchType.EMPTY_CELLS -> "Пустые ячейки"
                                },
                                maxLines = 1,
                                fontSize = 12.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        // Уменьшаем отступы внутри вкладки
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                    )
                }
            }
            
            // Компактная строка поиска
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Компактное поле ввода с кнопкой сканирования
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { query ->
                                viewModel.handleEvent(
                                    ProductInfoViewModel.UiEvent.OnSearchQueryChanged(query)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            label = {
                                Text(
                                    when (uiState.searchType) {
                                        ProductInfoViewModel.SearchType.LOCATION_ID -> "ШК ячейки"
                                        ProductInfoViewModel.SearchType.ARTICLE -> "Артикул"
                                        ProductInfoViewModel.SearchType.EMPTY_CELLS -> "Ячейки"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search, 
                                    contentDescription = "Поиск",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.handleEvent(
                                                ProductInfoViewModel.UiEvent.OnSearchQueryChanged("")
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear, 
                                            contentDescription = "Очистить",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Добавляем кнопку поиска
                    Button(
                        onClick = {
                            viewModel.handleEvent(ProductInfoViewModel.UiEvent.OnSearchClicked)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        enabled = uiState.searchQuery.isNotEmpty(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "НАЙТИ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Контейнер для результатов
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Отображение индикатора загрузки
                if (uiState.isLoading) {
                    LoadingIndicator()
                }
                // Отображение сообщения об ошибке
                else if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        elevation = 2.dp,
                        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Ошибка",
                                tint = MaterialTheme.colors.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colors.error,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                // Отображение сообщения, если ничего не найдено
                else if (uiState.isEmpty) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "По запросу ничего не найдено",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                // Отображение списка товаров в ячейке
                else if (uiState.locationProducts.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.locationProducts) { product ->
                            CompactLocationProductCard(product = product)
                        }
                        
                        // Нижний отступ для прокрутки
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                // Отображение информации о товаре по артикулу
                else if (uiState.productInfo != null) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Информация о товаре
                        item {
                            CompactProductInfoCard(productInfo = uiState.productInfo!!)
                        }
                        
                        // Заголовок списка мест хранения
                        item {
                            Text(
                                text = "Места хранения:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }
                        
                        // Список мест хранения
                        items(uiState?.productInfo?.items ?: emptyList()) { location ->
                            CompactProductLocationCard(location = location)
                        }
                        
                        // Нижний отступ для прокрутки
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }else if (uiState.searchType == ProductInfoViewModel.SearchType.EMPTY_CELLS && uiState.emptyCells.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.emptyCells) { cellName ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = cellName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

// Компактная карточка товара в ячейке
@Composable
fun CompactLocationProductCard(product: LocationProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Артикул и штрих-код в одну строку
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactInfoItem(label = "Арт:", value = product.article ?: "", modifier = Modifier.weight(1f))
                CompactInfoItem(label = "ШК:", value = product.shk ?: "", modifier = Modifier.weight(1f))
            }
            
            // Ячейка и количество в одну строку
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactInfoItem(label = "Ячейка:", value = product.wrShk ?: "", modifier = Modifier.weight(1f))
                
                // Получаем первый элемент из массива units, если он есть
                product.units.firstOrNull()?.let { unit ->
                    CompactInfoItem(
                        label = "Кол-во EХ:",     
                        value = try {
                            val pQnt = unit.productQnt.toIntOrNull() ?: 0
                            if (pQnt > 0) {
                                val result = unit.quantity.toFloat() / pQnt.toFloat()
                                result.toInt().toString()
                            } else "0"
                        } catch (e: Exception) {
                            "0"
                        },
                        modifier = Modifier.weight(1f)
                    )
                CompactInfoItem(
                    label = "Кол-во ед:",
                        value = unit.quantity.toString(),
                    modifier = Modifier.weight(1f)
                )
                } ?: run {
                    CompactInfoItem(label = "Кол-во EХ:", value = "0", modifier = Modifier.weight(1f))
                    CompactInfoItem(label = "Кол-во ед:", value = "0", modifier = Modifier.weight(1f))
                }
            }
            
            // Срок годности и состояние, если они есть
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val unit = product.units.firstOrNull()
                if (unit?.expirationDate != null) {
                    CompactInfoItem(label = "Годен до:", value = unit.expirationDate, modifier = Modifier.weight(1f))
                } else {
                    CompactInfoItem(label = "Годен до:", value = "Не указано", modifier = Modifier.weight(1f))
                }
                
                if (unit?.conditionState != null) {
                    CompactInfoItem(label = "Сост.:", value = unit.conditionState, modifier = Modifier.weight(1f))
                } else {
                    CompactInfoItem(label = "Сост.:", value = "Не указано", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// Компактная карточка с информацией о товаре по артикулу
@Composable
fun CompactProductInfoCard(productInfo: ProductInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(0xFFE3F2FD)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = productInfo.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Артикул и штрих-код в одну строку
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactInfoItem(label = "Арт:", value = productInfo.article ?: "", modifier = Modifier.weight(1f))
                CompactInfoItem(label = "ШК:", value = productInfo.shk ?: "", modifier = Modifier.weight(1f))
            }
            
            // Количество
            CompactInfoItem(label = "Общее кол-во:", value = productInfo.totalQuantity?.toString() ?: "0")
        }
    }
}

// Компактная карточка места хранения товара
@Composable
fun CompactProductLocationCard(location: ProductLocation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // ID ячейки и количество в одну строку
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactInfoItem(
                    label = "Ячейка:", 
                    value = location.name_wr ?: "",
                    valueStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                CompactInfoItem(
                    label = "Кол-во EX:",
                    value = try {
                        val pQnt = location.productQnt.toString().toIntOrNull() ?: 0
                        if (pQnt > 0) (location.quantity / pQnt).toString() else "0"
                    } catch (e: Exception) {
                        "0"
                    },
                    modifier = Modifier.weight(1f)
                )
                CompactInfoItem(
                    label = "Кол-во ед:",
                    value = location.quantity.toString(),
                    modifier = Modifier.weight(1f)
                )

            }
            
            // Срок годности и состояние в одну строку, если они есть
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (location.expirationDate != null) {
                    CompactInfoItem(label = "Годен до:", value = location.expirationDate, modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                if (location.conditionState != null) {
                    CompactInfoItem(label = "Сост.:", value = location.conditionState, modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// Компактная строка с информацией (заголовок и значение)
@Composable
fun CompactInfoItem(
    label: String,
    value: String,
    labelStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current.copy(
        fontSize = 12.sp,
        color = Color.Gray
    ),
    valueStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current.copy(
        fontSize = 12.sp
    ),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            style = valueStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
} 