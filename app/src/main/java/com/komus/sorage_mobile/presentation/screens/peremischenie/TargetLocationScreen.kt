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
fun TargetLocationScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    spHelper: SPHelper
) {
    var targetLocation by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val productId = spHelper.getProductId()
    val sourceLocation = spHelper.getSourceLocation()
    val quantity = spHelper.getQuantity()

    // Автозаполнение при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("TargetLocationScreen", "Сканирована целевая ячейка: $barcodeData")
            targetLocation = barcodeData
            scannerViewModel.clearBarcode()
            
            // Сохраняем целевую ячейку и переходим к экрану подтверждения
            spHelper.saveTargetLocation(targetLocation)
            navController.navigate("confirmation")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Перемещение товара", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = "ID продукта: $productId", style = MaterialTheme.typography.body1)
        Text(text = "Исходная ячейка: $sourceLocation", style = MaterialTheme.typography.body1)
        Text(text = "Количество: $quantity", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Отсканируйте ячейку, в которую перемещаете товар", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = targetLocation,
            onValueChange = { targetLocation = it.trim() },
            label = { Text("Код целевой ячейки") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (targetLocation.isNotEmpty()) {
                    spHelper.saveTargetLocation(targetLocation)
                    navController.navigate("confirmation")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = targetLocation.isNotEmpty()
        ) {
            Text("Продолжить")
        }
    }
} 