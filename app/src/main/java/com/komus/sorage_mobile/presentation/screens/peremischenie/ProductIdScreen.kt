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
fun ProductIdScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    spHelper: SPHelper
) {
    var productId by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()

    // Автозаполнение при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("ProductIdScreen", "Сканирован штрихкод: $barcodeData")
            productId = barcodeData
            scannerViewModel.clearBarcode()
            
            // Сохраняем ID продукта и переходим к следующему экрану
            spHelper.saveProductId(productId)
            navController.navigate("scan_source_location")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Перемещение товара", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Введите или отсканируйте ID продукта", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = productId,
            onValueChange = { productId = it.trim() },
            label = { Text("ID продукта") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (productId.isNotEmpty()) {
                    spHelper.saveProductId(productId)
                    navController.navigate("scan_source_location")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = productId.isNotEmpty()
        ) {
            Text("Продолжить")
        }
    }
} 