package com.komus.sorage_mobile.presentation.screens.snyatie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.state.PickState
import com.komus.sorage_mobile.domain.viewModel.PickViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuantityInputScreen(
    navController: NavController,
    pickViewModel: PickViewModel = hiltViewModel()
) {
    val selectedItem = pickViewModel.selectedItem
    val pickState by pickViewModel.pickState.collectAsStateWithLifecycle()
    var quantity by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // Если нет выбранного товара, возвращаемся назад
    if (selectedItem == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    // Сбрасываем состояние при входе на экран
    LaunchedEffect(Unit) {
        pickViewModel.resetPickState()
    }
    
    // Обработка состояния снятия товара
    LaunchedEffect(pickState) {
        when (pickState) {
            is PickState.Success -> {
                // Возвращаемся на главный экран через 2 секунды
                delay(2000)
                navController.navigate("removal") {
                    popUpTo("removal") { inclusive = true }
                }
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Снятие товара", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Информация о товаре
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = selectedItem.name,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Артикул: ${selectedItem.article}",
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Доступное количество: ${selectedItem.quantity}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Введите количество для снятия:",
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = quantity,
            onValueChange = { 
                // Проверяем, что введено число
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    quantity = it
                    validationError = null
                }
            },
            label = { Text("Количество") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        validationError?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (quantity.isNotEmpty()) {
                    val qnt = quantity.toInt()
                    if (qnt > selectedItem.quantity) {
                        // Показываем ошибку валидации
                        validationError = "Недостаточно товара в ячейке, введите меньшее значение"
                    } else {
                        // Снимаем товар
                        pickViewModel.pickProduct(qnt, "Оператор") // Здесь можно использовать имя пользователя из сессии
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = quantity.isNotEmpty() && pickState !is PickState.Loading
        ) {
            Text("Снять")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                // Устанавливаем максимальное количество
                quantity = selectedItem.quantity.toString()
                validationError = null
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
        ) {
            Text("Снять всё (${selectedItem.quantity})")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (pickState) {
            is PickState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PickState.Success -> {
                Text(
                    text = "Товар успешно снят!",
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.body1
                )
            }
            is PickState.Error -> {
                val errorMessage = (pickState as PickState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { pickViewModel.resetPickState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Повторить")
                }
            }
            else -> {}
        }
    }
} 