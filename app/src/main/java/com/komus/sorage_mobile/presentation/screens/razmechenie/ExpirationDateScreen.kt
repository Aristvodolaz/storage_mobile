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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var condition by remember { mutableStateOf("Кондиция") }
    var reason by remember { mutableStateOf("") }
    var showReasonError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showExpirationAlert by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Автоматический расчет конечной даты
    LaunchedEffect(startDate, days, months) {
        if (startDate.isNotEmpty() && (days.isNotEmpty() || months.isNotEmpty())) {
            endDate = viewModel.calculateEndDate(startDate, days, months)
        }
    }

    // Диалог предупреждения о сроке годности
    if (showExpirationAlert) {
        AlertDialog(
            onDismissRequest = { showExpirationAlert = false },
            title = { Text("Внимание!") },
            text = { 
                Column {
                    Text("Срок годности товара истек.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Для товара с истекшим сроком годности можно установить только состояние 'Некондиция'.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showExpirationAlert = false
                        condition = "Некондиция"
                    }
                ) {
                    Text("Установить 'Некондиция'")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpirationAlert = false }) {
                    Text("Отмена")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
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
            
            // Карточка выбора состояния товара
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
                    
                    // Радио-кнопки для выбора состояния
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            RadioButton(
                                selected = condition == "Кондиция",
                                onClick = { 
                                    condition = "Кондиция"
                                    reason = ""
                                    showReasonError = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colors.primary
                                )
                            )
                            Text(
                                "Кондиция",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = condition == "Некондиция",
                                onClick = { condition = "Некондиция" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colors.primary
                                )
                            )
                            Text(
                                "Некондиция",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    
                    // Выпадающий список причин некондиции
                    if (condition == "Некондиция") {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = reason,
                                onValueChange = {},
                                readOnly = true,
                                label = { 
                                    Text(
                                        "Причина некондиции",
                                        fontSize = 10.sp
                                    ) 
                                },
                                trailingIcon = {
                                    Icon(
                                        if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        "Развернуть",
                                        Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                isError = showReasonError && reason.isEmpty()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                ConditionReasons.reasons.forEach { reasonOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            reason = reasonOption
                                            expanded = false
                                            showReasonError = false
                                        }
                                    ) {
                                        Text(
                                            text = reasonOption,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (showReasonError && reason.isEmpty()) {
                            Text(
                                text = "Выберите причину некондиции",
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Кнопка сохранения
            Button(
                onClick = {
                    if (condition == "Некондиция" && reason.isEmpty()) {
                        showReasonError = true
                        return@Button
                    }

                    // Проверяем срок годности перед сохранением
                    val isoDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            val date = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            date.format(DateTimeFormatter.ISO_DATE)
                        } catch (e: Exception) {
                            Log.e("ExpirationDateScreen", "Ошибка преобразования даты: ${e.message}")
                            ""
                        }
                    } else {
                        DateUtils.convertToIsoFormat(endDate)
                    }

                    if (isoDate.isNotEmpty()) {
                        val validatedIsoDate = ProductMovementHelper.processExpirationDate(isoDate)
                        if (ExpirationDateValidator.isExpired(validatedIsoDate) && condition == "Кондиция") {
                            showExpirationAlert = true
                            return@Button
                        }
                    }
                    
                    viewModel.saveExpirationData(
                        startDate = startDate,
                        days = days,
                        months = months,
                        condition = condition,
                        reason = reason
                    )
                    navController.navigate("scan_ir_location") {
                        popUpTo("expiration_date") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(36.dp),
                enabled = startDate.isNotEmpty() && endDate.isNotEmpty() && 
                         (condition == "Кондиция" || (condition == "Некондиция" && reason.isNotEmpty())),
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