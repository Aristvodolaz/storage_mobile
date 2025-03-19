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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (showQuantityInput) "Введите количество" else "Выберите единицу", 
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                modifier = Modifier.height(44.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
        when (unitState) {
                is UnitState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            is UnitState.Success -> {
                val units = (unitState as UnitState.Success).data
                    
                    if (!showQuantityInput) {
                        // Экран выбора единицы хранения
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(4.dp),
                            backgroundColor = Color(0xFFF5F5F5)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Доступные единицы хранения:",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(units) { unit ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    elevation = 1.dp,
                                    shape = RoundedCornerShape(4.dp),
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
                                        elevation = ButtonDefaults.elevation(0.dp),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = unit.typeName,
                                                    style = MaterialTheme.typography.caption,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Кол-во в упаковке: ${unit.productQnt}",
                                                    style = MaterialTheme.typography.caption,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Выбрать",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Экран ввода количества
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                Text(
                                    text = "Выбрана единица:",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = selectedUnit?.typeName ?: "",
                                    style = MaterialTheme.typography.body2,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Text(
                                    text = "Количество в упаковке: ${selectedUnit?.productQnt}",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { 
                                        // Проверяем, что введено число
                                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                            quantity = it
                                        }
                                    },
                                    label = { 
                                        Text(
                                            "Количество упаковок",
                                            fontSize = 10.sp
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                if (quantity.isNotEmpty()) {
                                    val totalItems = quantity.toInt() * (selectedUnit?.productQnt ?: 1)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 0.dp,
                                        backgroundColor = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = "Общее количество товара:",
                                                style = MaterialTheme.typography.caption,
                                                fontSize = 10.sp
                                            )
                                            
                                            Text(
                                                text = "$totalItems шт.",
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.primary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "НАЗАД",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                    Button(
                        onClick = {
                            selectedUnit?.let {
                                if(quantity.isNotEmpty()) {
                                    val fullQnt = quantity.toInt() * selectedUnit!!.productQnt
                                            // Сохраняем информацию о единице хранения
                                            spHelper.saveBrief(selectedUnit!!.type.toString())
                                            // Сохраняем артикул товара
                                            spHelper.saveProductId(productId)
                                    spHelper.saveProductQnt(selectedUnit!!.productQnt)
                                            // Сохраняем количество
                                    spHelper.saveFullQnt(fullQnt)
                                            // Переходим на экран ExprationDate
                                            navController.navigate("expiration_date")
                                        }
                                    }
                                },
                                enabled = quantity.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "ДАЛЕЕ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Подтвердить",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                is UnitState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = 2.dp,
                        backgroundColor = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Ошибка",
                                style = MaterialTheme.typography.subtitle2,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = (unitState as UnitState.Error).message,
                                style = MaterialTheme.typography.caption,
                                color = Color.Red,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { unitViewModel.fetchUnits(productId) },
                                modifier = Modifier.align(Alignment.End),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "ПОВТОРИТЬ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
