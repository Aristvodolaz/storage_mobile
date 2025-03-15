package com.komus.sorage_mobile.presentation.screens.snyatie

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.domain.state.LocationItemsState
import com.komus.sorage_mobile.domain.viewModel.PickViewModel

@Composable
fun ScanLocationScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    pickViewModel: PickViewModel
) {
    var locationId by remember { mutableStateOf("") }
    var skladId by remember { mutableStateOf("85") } // Значение по умолчанию
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val locationItemsState by pickViewModel.locationItemsState.collectAsStateWithLifecycle()
    var scanMode by remember { mutableStateOf(ScanMode.LOCATION) } // Режим сканирования: ячейка или товар
    
    // Очищаем состояние при входе на экран
    LaunchedEffect(Unit) {
        pickViewModel.resetLocationItemsState()
        // Устанавливаем ID склада
        pickViewModel.setSkladId(skladId)
    }
    
    // Обновляем ID склада при его изменении
    LaunchedEffect(skladId) {
        pickViewModel.setSkladId(skladId)
    }
    
    // Автозаполнение при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            val scannedCode = barcodeData
            scannerViewModel.clearBarcode()
            
            when (scanMode) {
                ScanMode.LOCATION -> {
                    Log.d("ScanLocationScreen", "Сканирована ячейка: $scannedCode")
                    locationId = scannedCode
                    
                    // Запрашиваем товары в ячейке
                    pickViewModel.getLocationItems(locationId)
                }
                ScanMode.PRODUCT -> {
                    Log.d("ScanLocationScreen", "Сканирован товар: $scannedCode")
                    // Ищем товар по штрихкоду
                    pickViewModel.searchProductByBarcode(scannedCode)
                }
            }
        }
    }
    
    // Обработка состояния получения товаров
    LaunchedEffect(locationItemsState) {
        when (locationItemsState) {
            is LocationItemsState.Success -> {
                val items = (locationItemsState as LocationItemsState.Success).items
                if (items.size == 1) {
                    // Если в ячейке только один товар, сразу выбираем его и переходим к экрану количества
                    pickViewModel.selectItem(items[0])
                    navController.navigate("pick_quantity")
                } else if (items.size > 1) {
                    // Если в ячейке несколько товаров, переходим к экрану выбора товара
                    navController.navigate("pick_item_selection")
                }
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Снятие товара", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Переключатель режима сканирования
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TabRow(
                selectedTabIndex = scanMode.ordinal,
                modifier = Modifier.fillMaxWidth()
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (scanMode) {
            ScanMode.LOCATION -> {
                Text(
                    text = "Отсканируйте или введите ШК ячейки",
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = locationId,
                    onValueChange = { locationId = it.trim() },
                    label = { Text("ШК ячейки") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = skladId,
                    onValueChange = { skladId = it.trim() },
                    label = { Text("ID склада") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (locationId.isNotEmpty()) {
                            pickViewModel.getLocationItems(locationId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = locationId.isNotEmpty()
                ) {
                    Text("Продолжить")
                }
            }
            ScanMode.PRODUCT -> {
                Text(
                    text = "Отсканируйте штрихкод товара",
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = "",
                    onValueChange = { /* Не используется, только сканирование */ },
                    label = { Text("Штрихкод товара") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Для поиска товара отсканируйте его штрихкод",
                    style = MaterialTheme.typography.caption
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (locationItemsState) {
            is LocationItemsState.Loading -> {
                CircularProgressIndicator()
                Text(
                    text = "Загрузка товаров...",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is LocationItemsState.Error -> {
                val errorMessage = (locationItemsState as LocationItemsState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body2
                )
            }
            else -> {}
        }
    }
}

enum class ScanMode {
    LOCATION, PRODUCT
} 