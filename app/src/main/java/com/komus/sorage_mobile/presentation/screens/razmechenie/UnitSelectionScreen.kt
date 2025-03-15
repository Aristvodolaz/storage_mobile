package com.komus.sorage_mobile.presentation.screens.razmechenie

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.komus.sorage_mobile.data.response.UnitItem
import com.komus.sorage_mobile.domain.state.UnitState
import com.komus.sorage_mobile.domain.viewModel.UnitViewModel
import com.komus.sorage_mobile.util.SPHelper

@Composable
fun UnitSelectionScreen(
    navController: NavController,
    productId: String,
    spHelper: SPHelper,
    unitViewModel: UnitViewModel = hiltViewModel()
) {
    val unitState by unitViewModel.unitState.collectAsState()
    var selectedUnit by remember { mutableStateOf<UnitItem?>(null) }
    var quantity by remember { mutableStateOf("") }
    var showQuantityInput by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(productId) {
        unitViewModel.fetchUnits(productId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Заголовок
        Text(
            text = if (showQuantityInput) "Введите количество" else "Выберите единицу хранения", 
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (unitState) {
            is UnitState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UnitState.Success -> {
                val units = (unitState as UnitState.Success).data
                
                if (!showQuantityInput) {
                    // Экран выбора единицы хранения с прокруткой
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(units) { unit ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp),
                                backgroundColor = if (selectedUnit == unit) Color(0xFFE0E0FF) else MaterialTheme.colors.surface
                            ) {
                                Button(
                                    onClick = { 
                                        selectedUnit = unit
                                        showQuantityInput = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (selectedUnit == unit) Color(0xFFE0E0FF) else MaterialTheme.colors.surface,
                                        contentColor = MaterialTheme.colors.onSurface
                                    ),
                                    elevation = ButtonDefaults.elevation(0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = unit.typeName,
                                                style = MaterialTheme.typography.body1,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Количество в упаковке: ${unit.productQnt}",
                                                style = MaterialTheme.typography.body2
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Выбрать"
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Экран ввода количества с прокруткой
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Выбрана единица: ${selectedUnit?.typeName}",
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Количество в упаковке: ${selectedUnit?.productQnt}",
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { 
                                        // Проверяем, что введено число
                                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                            quantity = it
                                        }
                                    },
                                    label = { Text("Количество упаковок") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (quantity.isNotEmpty()) {
                                    val totalItems = quantity.toInt() * (selectedUnit?.productQnt ?: 1)
                                    Text(
                                        text = "Общее количество товара: $totalItems шт.",
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопки навигации
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (showQuantityInput) {
                        Button(
                            onClick = { showQuantityInput = false },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.LightGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Назад")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                selectedUnit?.let {
                                    if(quantity.isNotEmpty()) {
                                        val fullQnt = quantity.toInt() * selectedUnit!!.productQnt
                                        // Сохраняем информацию о единице хранения
                                        spHelper.saveBrief(selectedUnit!!.typeName)
                                        // Сохраняем артикул товара
                                        spHelper.saveProductId(productId)
                                        // Сохраняем количество
                                        spHelper.saveFullQnt(fullQnt)
                                        // Переходим на экран ExprationDate
                                        navController.navigate("expiration_date")
                                    }
                                }
                            },
                            enabled = quantity.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Далее")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Подтвердить"
                            )
                        }
                    }
                }
            }
            is UnitState.Error -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 4.dp,
                    backgroundColor = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Ошибка",
                            style = MaterialTheme.typography.h6,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (unitState as UnitState.Error).message,
                            style = MaterialTheme.typography.body1,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { unitViewModel.fetchUnits(productId) },
                            modifier = Modifier.align(Alignment.End)
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
