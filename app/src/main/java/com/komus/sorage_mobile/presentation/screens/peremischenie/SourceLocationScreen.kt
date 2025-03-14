package com.komus.sorage_mobile.presentation.screens.peremischenie

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.util.SPHelper

@Composable
fun SourceLocationScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    spHelper: SPHelper
) {
    var sourceLocation by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val productId = spHelper.getProductId()

    // Автозаполнение при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("SourceLocationScreen", "Сканирована ячейка: $barcodeData")
            sourceLocation = barcodeData
            scannerViewModel.clearBarcode()
            
            // Сохраняем исходную ячейку и переходим к следующему экрану
            spHelper.saveSourceLocation(sourceLocation)
            navController.navigate("quantity_input")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Перемещение товара", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = "ID продукта: $productId", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Отсканируйте ячейку, из которой перемещаете товар", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = sourceLocation,
            onValueChange = { sourceLocation = it.trim() },
            label = { Text("Код ячейки") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (sourceLocation.isNotEmpty()) {
                    spHelper.saveSourceLocation(sourceLocation)
                    navController.navigate("quantity_input")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = sourceLocation.isNotEmpty()
        ) {
            Text("Продолжить")
        }
    }
} 