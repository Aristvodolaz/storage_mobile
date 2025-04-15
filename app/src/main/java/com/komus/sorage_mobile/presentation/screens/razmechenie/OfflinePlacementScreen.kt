package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TabRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.sorage_mobile.domain.viewModel.OfflinePlacementViewModel
import com.komus.sorage_mobile.domain.viewModel.PlacementState
import com.komus.sorage_mobile.domain.viewModel.ProductState
import androidx.compose.material.OutlinedTextField

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OfflinePlacementScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    offlinePlacementViewModel: OfflinePlacementViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    var currentStep by remember { mutableStateOf(0) }
    var articleOrBarcode by remember { mutableStateOf("") }
    var isArticleMode by remember { mutableStateOf(true) }
    var selectedPrunitType by remember { mutableStateOf<Int?>(null) }
    var quantity by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var expirationPeriod by remember { mutableStateOf("") }
    var isMonthMode by remember { mutableStateOf(true) }
    var isGoodCondition by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("") }
    var cellBarcode by remember { mutableStateOf("") }
    var productQnt by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var startDateInput by remember { mutableStateOf(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) }

    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val placementState by offlinePlacementViewModel.placementState.collectAsStateWithLifecycle()
    val productState by offlinePlacementViewModel.productState.collectAsStateWithLifecycle()

    // Обработка отсканированного штрихкода
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            when (currentStep) {
                0 -> if (!isArticleMode) articleOrBarcode = barcodeData
                3 -> cellBarcode = barcodeData
            }
            scannerViewModel.clearBarcode()
        }
    }

    // Обработка состояния размещения
    LaunchedEffect(placementState) {
        when (placementState) {
            is PlacementState.Success -> {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = "Размещение успешно сохранено",
                    duration = SnackbarDuration.Short
                )
                navController.popBackStack()
            }
            is PlacementState.Error -> {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = (placementState as PlacementState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    val prunitTypes = mapOf(
        0 to PrunitType("не указан", "не указан"),
        1 to PrunitType("Единица", "Ед"),
        2 to PrunitType("Минимальная Упаковка", "Мин.Уп"),
        3 to PrunitType("Промежуточная Упаковка", "Пр.Уп"),
        10 to PrunitType("Фабричная Упаковка", "Фб.Уп"),
        11 to PrunitType("Паллет", "Паллет")
    )

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Оффлайн размещение",
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Индикатор прогресса
            LinearProgressIndicator(
                progress = (currentStep + 1) / 4f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Шаг 1: Ввод артикула или штрихкода
            if (currentStep == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Шаг 1: Идентификация товара",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TabRow(
                            selectedTabIndex = if (isArticleMode) 0 else 1,
                            backgroundColor = Color.Transparent
                        ) {
                            Tab(
                                selected = isArticleMode,
                                onClick = { isArticleMode = true },
                                text = { Text("Артикул") }
                            )
                            Tab(
                                selected = !isArticleMode,
                                onClick = { isArticleMode = false },
                                text = { Text("Штрихкод") }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = articleOrBarcode,
                            onValueChange = { articleOrBarcode = it },
                            label = { Text(if (isArticleMode) "Введите артикул" else "Отсканируйте штрихкод") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        if (articleOrBarcode.isNotEmpty()) {
                            Button(
                                onClick = {
//                                    offlinePlacementViewModel.findProduct(articleOrBarcode, isArticleMode)
                                          },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Найти товар")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (val state = productState) {
                                is ProductState.Found -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 2.dp,
                                        backgroundColor = Color(0xFFE8F5E9)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = state.product.name,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("Артикул: ${state.product.article}")
                                            Text("Штрихкод: ${state.product.shk}")
                                            Text("Количество: ${state.product.productQnt}")
                                            Text("Тип упаковки: ${state.product.prunitName}")
                                            Text("Состояние: ${state.product.conditionState}")
                                        }
                                    }
                                }
                                is ProductState.NotFound -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 2.dp,
                                        backgroundColor = Color(0xFFFFEBEE)
                                    ) {
                                        Text(
                                            text = "Товар не найден",
                                            modifier = Modifier.padding(8.dp),
                                            color = Color.Red
                                        )
                                    }
                                }
                                is ProductState.Error -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 2.dp,
                                        backgroundColor = Color(0xFFFFEBEE)
                                    ) {
                                        Text(
                                            text = "Ошибка: ${state.message}",
                                            modifier = Modifier.padding(8.dp),
                                            color = Color.Red
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { currentStep++ },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = articleOrBarcode.isNotEmpty()
                        ) {
                            Text("Далее")
                        }
                    }
                }
            }

// Шаг 2: Выбор типа укладки с добавленным скроллом
            else if (currentStep == 1) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Шаг 2: Тип укладки",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(prunitTypes.toList()) { (id, type) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedPrunitType == id,
                                            onClick = { selectedPrunitType = id }
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedPrunitType == id,
                                        onClick = { selectedPrunitType = id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(type.name)
                                        Text(
                                            type.brief,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { currentStep-- }
                            ) {
                                Text("Назад")
                            }

                            Button(
                                onClick = { currentStep++ },
                                enabled = selectedPrunitType != null
                            ) {
                                Text("Далее")
                            }
                        }
                    }
                }
            }


            // Шаг 3: Ввод количества и срока годности с самостоятельным вводом начальной даты
            else if (currentStep == 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Шаг 3: Количество и срок годности",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                                    label = { Text("Количество товара") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (selectedPrunitType != null && selectedPrunitType != 0 && selectedPrunitType != 1) {
                                    OutlinedTextField(
                                        value = productQnt,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) productQnt = it },
                                        label = { Text("Количество в типе укладки") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                OutlinedTextField(
                                    value = startDateInput,
                                    onValueChange = { input ->
                                        startDateInput = input
                                        runCatching {
                                            startDate = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                        }
                                    },
                                    label = { Text("Начальная дата (dd.MM.yyyy)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    modifier = Modifier.fillMaxWidth()
                                )



                                Spacer(modifier = Modifier.height(16.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = months,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) months = it },
                                        label = { Text("Месяцы") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedTextField(
                                        value = days,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) days = it },
                                        label = { Text("Дни") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                val parsedStartDate = runCatching {
                                    LocalDate.parse(startDateInput, formatter)
                                }.getOrNull() ?: LocalDate.now()
                                val totalMonths = months.toIntOrNull() ?: 0
                                val totalDays = days.toIntOrNull() ?: 0
                                val endDate = parsedStartDate.plusMonths(totalMonths.toLong()).plusDays(totalDays.toLong())

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Конечная дата: ${endDate.format(formatter)}",
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Состояние товара")
                                Row {
                                    RadioButton(
                                        selected = isGoodCondition,
                                        onClick = { isGoodCondition = true }
                                    )
                                    Text("Кондиция")

                                    Spacer(modifier = Modifier.width(8.dp))

                                    RadioButton(
                                        selected = !isGoodCondition,
                                        onClick = { isGoodCondition = false }
                                    )
                                    Text("Некондиция")
                                }

                                if (!isGoodCondition) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = reason,
                                        onValueChange = { reason = it },
                                        label = { Text("Причина некондиции") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { currentStep-- }
                            ) {
                                Text("Назад")
                            }

                            Button(
                                onClick = { currentStep++ },
                                enabled = quantity.isNotEmpty() && (selectedPrunitType == null || selectedPrunitType == 0 || selectedPrunitType == 1 || productQnt.isNotEmpty()) &&
                                        (isGoodCondition || (!isGoodCondition && reason.isNotEmpty()))
                            ) {
                                Text("Далее")
                            }
                        }
                    }
                }
            }
            // Шаг 4: Сканирование ячейки
            else if (currentStep == 3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Шаг 4: Сканирование ячейки",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = cellBarcode,
                            onValueChange = { cellBarcode = it },
                            label = { Text("Отсканируйте штрихкод ячейки") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { currentStep-- }
                            ) {
                                Text("Назад")
                            }
                            
                            Button(
                                onClick = {
                                    val period = expirationPeriod.toIntOrNull() ?: 0
                                    val endDate = if (isMonthMode) {
                                        startDate.plusMonths(period.toLong())
                                    } else {
                                        startDate.plusDays(period.toLong())
                                    }

                                    offlinePlacementViewModel.savePlacement(
                                        articleOrBarcode = articleOrBarcode,
                                        isArticleMode = isArticleMode,
                                        prunitTypeId = selectedPrunitType ?: 0,
                                        quantity = quantity.toIntOrNull() ?: 0,
                                        startDate = startDate,
                                        endDate = endDate,
                                        isGoodCondition = isGoodCondition,
                                        reason = if (!isGoodCondition) reason else null,
                                        cellBarcode = cellBarcode,
                                        productQnt = productQnt.toIntOrNull() ?: 0
                                    )

                                },
                                enabled = cellBarcode.isNotEmpty()
                            ) {
                                Text("Завершить")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PrunitType(
    val name: String,
    val brief: String
) 