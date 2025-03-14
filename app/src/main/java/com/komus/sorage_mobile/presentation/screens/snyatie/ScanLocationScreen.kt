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
    pickViewModel: PickViewModel = hiltViewModel()
) {
    var locationId by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val locationItemsState by pickViewModel.locationItemsState.collectAsStateWithLifecycle()
    
    // Очищаем состояние при входе на экран
    LaunchedEffect(Unit) {
        pickViewModel.resetLocationItemsState()
    }
    
    // Автозаполнение при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("ScanLocationScreen", "Сканирована ячейка: $barcodeData")
            locationId = barcodeData
            scannerViewModel.clearBarcode()
            
            // Запрашиваем товары в ячейке
            pickViewModel.getLocationItems(locationId)
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (locationItemsState) {
            is LocationItemsState.Loading -> {
                CircularProgressIndicator()
                Text(
                    text = "Загрузка товаров в ячейке...",
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