package com.komus.sorage_mobile.presentation.screens.snyatie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.state.PickState
import com.komus.sorage_mobile.domain.viewModel.PickViewModel
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuantityInputScreen(
    navController: NavController,
    pickViewModel: PickViewModel = hiltViewModel(),
    spHelper: SPHelper = hiltViewModel()
) {
    val selectedItem = pickViewModel.selectedItem
    val pickState by pickViewModel.pickState.collectAsStateWithLifecycle()
    var quantity by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
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
                // Сохраняем данные о снятии товара
                spHelper.saveProductId(selectedItem.productId)
                spHelper.saveProductName(selectedItem.name)
                spHelper.saveArticle(selectedItem.article)
                spHelper.saveQuantity(quantity.toInt())
                
                // Возвращаемся на главный экран через 2 секунды
                delay(2000)
                navController.navigate("removal") {
                    popUpTo("removal") { inclusive = true }
                }
            }
            else -> {}
        }
    }
    
    // Диалог подтверждения снятия товара
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Подтверждение") },
            text = {
                Column {
                    Text("Вы уверены, что хотите снять:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedItem.name,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Артикул: ${selectedItem.article}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Количество: $quantity шт.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        // Снимаем товар с использованием нового метода
                        pickViewModel.pickFromLocation(
                            quantity.toInt(),
                            spHelper.getUserName() // Используем имя пользователя из сессии
                        )
                    }
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text("Отмена")
                }
            }
        )
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
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Артикул: ${selectedItem.article}",
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ячейка: ${selectedItem.prunitId.split("-")[0]}",
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Доступное количество: ${selectedItem.quantity} шт.",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Ошибка",
                    tint = MaterialTheme.colors.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (quantity.isNotEmpty()) {
                    val qnt = quantity.toInt()
                    if (qnt <= 0) {
                        validationError = "Количество должно быть больше нуля"
                    } else if (qnt > selectedItem.quantity) {
                        // Показываем ошибку валидации
                        validationError = "Недостаточно товара в ячейке, введите меньшее значение"
                    } else {
                        // Показываем диалог подтверждения
                        showConfirmDialog = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = quantity.isNotEmpty() && pickState !is PickState.Loading,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Снять",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Снять товар")
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
            Text("Снять всё (${selectedItem.quantity} шт.)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (pickState) {
            is PickState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Выполняется снятие товара...",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            is PickState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFE8F5E9),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Успешно",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Товар успешно снят!",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            is PickState.Error -> {
                val errorMessage = (pickState as PickState.Error).message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFFFEBEE),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Ошибка",
                                tint = MaterialTheme.colors.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ошибка снятия товара",
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { pickViewModel.resetPickState() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }
            else -> {}
        }
    }
} 