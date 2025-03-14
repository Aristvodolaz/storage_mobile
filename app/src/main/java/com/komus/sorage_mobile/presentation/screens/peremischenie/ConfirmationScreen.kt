package com.komus.sorage_mobile.presentation.screens.peremischenie

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.viewModel.MovementViewModel
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.launch

@Composable
fun ConfirmationScreen(
    navController: NavController,
    spHelper: SPHelper,
    movementViewModel: MovementViewModel = hiltViewModel()
) {
    val productId = spHelper.getProductId()
    val sourceLocation = spHelper.getSourceLocation()
    val targetLocation = spHelper.getTargetLocation()
    val quantity = spHelper.getQuantity()
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Подтверждение перемещения", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Информация о перемещении:", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = "ID продукта: $productId", style = MaterialTheme.typography.body1)
                Text(text = "Исходная ячейка: $sourceLocation", style = MaterialTheme.typography.body1)
                Text(text = "Целевая ячейка: $targetLocation", style = MaterialTheme.typography.body1)
                Text(text = "Количество: $quantity", style = MaterialTheme.typography.body1)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
        }
        
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        successMessage?.let {
            Text(text = it, color = MaterialTheme.colors.primary, style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    navController.popBackStack("product_id_input", false)
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Отмена")
            }
            
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    
                    // Вызываем метод для отправки данных о перемещении
                    movementViewModel.moveProduct(
                        productId = productId,
                        sourceLocationId = sourceLocation,
                        targetLocationId = targetLocation,
                        quantity = quantity,
                        onSuccess = {
                            isLoading = false
                            successMessage = "Товар успешно перемещен"
                            // Очищаем данные после успешного перемещения
                            spHelper.clearMovementData()
                            // Возвращаемся на главный экран через 2 секунды
                            kotlinx.coroutines.MainScope().launch {
                                kotlinx.coroutines.delay(2000)
                                navController.navigate("movement") {
                                    popUpTo("movement") { inclusive = true }
                                }
                            }
                        },
                        onError = { message ->
                            isLoading = false
                            errorMessage = "Ошибка: $message"
                        }
                    )
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                enabled = !isLoading
            ) {
                Text("Подтвердить")
            }
        }
    }
} 