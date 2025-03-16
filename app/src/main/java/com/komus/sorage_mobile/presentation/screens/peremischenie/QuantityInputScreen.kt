package com.komus.sorage_mobile.presentation.screens.peremischenie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.viewModel.PickViewModel
import com.komus.sorage_mobile.util.SPHelper

@Composable
fun QuantityInputScreen(
    navController: NavController,
    spHelper: SPHelper
) {
    var quantity by remember { mutableStateOf("") }
    val productId = spHelper.getProductId()
    val sourceLocation = spHelper.getSourceLocation()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Перемещение товара", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = "ID продукта: $productId", style = MaterialTheme.typography.body1)
        Text(text = "Исходная ячейка: $sourceLocation", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Введите количество товара для перемещения", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = quantity,
            onValueChange = { 
                // Проверяем, что введено число
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    quantity = it
                }
            },
            label = { Text("Количество") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (quantity.isNotEmpty()) {
                    spHelper.saveQuantity(quantity.toInt())
                    navController.navigate("scan_target_location")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = quantity.isNotEmpty()
        ) {
            Text("Продолжить")
        }
    }
} 