package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.viewModel.ExpirationDateViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpirationDateScreen(
    navController: NavController,
    viewModel: ExpirationDateViewModel = hiltViewModel()
) {
    var startDate by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf<String?>(null) }
    var reason by remember { mutableStateOf("") }
    var showReasonError by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Автоматический расчет конечной даты
    LaunchedEffect(startDate, days, months) {
        if (startDate.isNotEmpty() && (days.isNotEmpty() || months.isNotEmpty())) {
            endDate = viewModel.calculateEndDate(startDate, days, months)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Срок годности", 
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
                .verticalScroll(scrollState)
        ) {
            // Карточка для ввода даты
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Дата производства",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { 
                            Text(
                                "Начальная дата (дд.мм.гггг)",
                                fontSize = 10.sp
                            ) 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Календарь",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            
            // Карточка для ввода срока хранения
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Срок хранения",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row {
                        OutlinedTextField(
                            value = days,
                            onValueChange = { 
                                // Проверяем, что введено число
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    days = it
                                }
                            },
                            label = { 
                                Text(
                                    "Дни",
                                    fontSize = 10.sp
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        OutlinedTextField(
                            value = months,
                            onValueChange = { 
                                // Проверяем, что введено число
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    months = it
                                }
                            },
                            label = { 
                                Text(
                                    "Месяцы",
                                    fontSize = 10.sp
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Карточка с результатом расчета
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp),
                backgroundColor = if (endDate.isNotEmpty()) Color(0xFFE8F5E9) else MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Дата окончания срока годности",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = {},
                        label = { 
                            Text(
                                "Конечная дата",
                                fontSize = 10.sp
                            ) 
                        },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Дата окончания",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            
            // Карточка для выбора состояния
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Состояние товара",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = condition == "Кондиция",
                                onClick = { 
                                    condition = "Кондиция"
                                    showReasonError = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colors.primary
                                ),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Кондиция",
                                fontSize = 10.sp
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = condition == "Некондиция",
                                onClick = { 
                                    condition = "Некондиция"
                                    showReasonError = reason.isEmpty()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colors.primary
                                ),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Некондиция",
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Поле для ввода причины некондиции
                    if (condition == "Некондиция") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { 
                                reason = it
                                showReasonError = it.isEmpty()
                            },
                            label = { 
                                Text(
                                    "Причина некондиции",
                                    fontSize = 10.sp
                                ) 
                            },
                            isError = showReasonError,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            singleLine = true
                        )
                        if (showReasonError) {
                            Text(
                                text = "Укажите причину некондиции",
                                color = MaterialTheme.colors.error,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
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
                Button(
                    onClick = { navController.popBackStack() },
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
                        if (condition == "Некондиция" && reason.isEmpty()) {
                            showReasonError = true
                        } else {
                            viewModel.saveExpirationData(
                                startDate = startDate,
                                days = days,
                                months = months,
                                condition = condition ?: "",
                                reason = if (condition == "Некондиция") reason else null
                            )
                            navController.navigate("product_info")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = startDate.isNotEmpty() && condition != null && 
                            (condition != "Некондиция" || (condition == "Некондиция" && reason.isNotEmpty())),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ДАЛЕЕ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            // Добавляем дополнительное пространство внизу для удобства прокрутки
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}