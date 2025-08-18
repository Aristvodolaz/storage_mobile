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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.viewModel.ExpirationDateViewModel
import com.komus.sorage_mobile.data.model.ConditionReasons
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import com.komus.sorage_mobile.util.DateUtils
import com.komus.sorage_mobile.util.ProductMovementHelper

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.util.Log

@OptIn(ExperimentalMaterialApi::class)
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

    var skipExpirationDate by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val startDateFocusRequester = remember { FocusRequester() }
    val daysFocusRequester = remember { FocusRequester() }
    val monthsFocusRequester = remember { FocusRequester() }

    // Автоматический расчет конечной даты
    LaunchedEffect(startDate, days, months) {
        if (!skipExpirationDate && startDate.isNotEmpty() && (days.isNotEmpty() || months.isNotEmpty())) {
            endDate = viewModel.calculateEndDate(startDate, days, months)
        }
    }
    
    // Автофокус на поле даты при загрузке экрана
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Небольшая задержка для инициализации UI
        if (!skipExpirationDate) {
            startDateFocusRequester.requestFocus()
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
                        text = if (skipExpirationDate) "Срок годности (пропущен)" else "Дата производства",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (!skipExpirationDate) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(startDateFocusRequester),
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
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    // Устанавливаем дату 01.01.2999 и очищаем поля
                                    startDate = ""
                                    days = ""
                                    months = ""
                                    endDate = "01.01.2999"
                                    skipExpirationDate = true
                                    
                                    // Автоматически выполняем логику сохранения
                                    val finalCondition = "Кондиция"
                                    val finalReason = ""
                                    
                                    viewModel.saveExpirationData(
                                        startDate = "01.01.2999",
                                        days = "",
                                        months = "",
                                        condition = finalCondition,
                                        reason = finalReason
                                    )
                                    
                                    // Переходим на следующий экран
                                    navController.navigate("scan_buffer_location") {
                                        popUpTo("expiration_date") { inclusive = true }
                                    }
                                },
                                modifier = Modifier.height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    "Пропустить СГ",
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Дата: 01.01.2999 (пропущен)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                skipExpirationDate = false
                                endDate = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Ввести дату вручную", fontSize = 10.sp)
                        }
                    }
                }
            }
            
            // Карточка для ввода срока хранения
            if (!skipExpirationDate) {
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
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(daysFocusRequester),
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
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(monthsFocusRequester),
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
            }
            


            // Кнопка сохранения
            Button(
                onClick = {
                    // Устанавливаем состояние "Кондиция" по умолчанию
                    val finalCondition = "Кондиция"
                    val finalReason = ""
                    
                    // Сохраняем данные
                    val finalStartDate = if (skipExpirationDate) "01.01.2999" else startDate
                    val finalDays = if (skipExpirationDate) "" else days
                    val finalMonths = if (skipExpirationDate) "" else months
                    
                    viewModel.saveExpirationData(
                        startDate = finalStartDate,
                        days = finalDays,
                        months = finalMonths,
                        condition = finalCondition,
                        reason = finalReason
                    )
                                            navController.navigate("scan_buffer_location") {
                            popUpTo("expiration_date") { inclusive = true }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(36.dp),
                enabled = skipExpirationDate || (startDate.isNotEmpty() && endDate.isNotEmpty()),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "СОХРАНИТЬ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}