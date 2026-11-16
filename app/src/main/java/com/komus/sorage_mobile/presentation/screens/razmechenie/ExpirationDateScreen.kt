package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.data.model.ConditionReasons
import com.komus.sorage_mobile.domain.viewModel.ExpirationDateViewModel
import com.komus.sorage_mobile.presentation.components.PlacementProductHeader

private val GoodColor = Color(0xFF2E7D32)
private val BadColor = Color(0xFFE65100)

/** Дата вводится цифрами; точки ставятся сами: 31.12.2025. */
internal fun formatDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { index, c ->
            if (index == 2 || index == 4) append('.')
            append(c)
        }
    }
}

/**
 * Срок годности и состояние товара.
 * Оператор выбирает состояние, при некондиции — причину, затем срок: дату производства и срок хранения,
 * либо «без срока». Переход дальше только если данные прошли проверку.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpirationDateScreen(
    navController: NavController,
    viewModel: ExpirationDateViewModel = hiltViewModel()
) {
    var condition by remember { mutableStateOf("Кондиция") }
    var reason by remember { mutableStateOf("") }
    var skipExpiration by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    // Курсор всегда в конце после форматирования точками, иначе цифры вставляются не на место
    var startDateField by remember { mutableStateOf(TextFieldValue("")) }
    var days by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val isNonConformity = condition == "Некондиция"
    val endDate = if (!skipExpiration) viewModel.calculateEndDate(startDate, days, months) else ""
    val canSave = (!isNonConformity || reason.isNotBlank()) &&
        (skipExpiration || endDate.isNotEmpty())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Состояние и срок", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                modifier = Modifier.height(52.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            PlacementProductHeader()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                SectionTitle("Состояние")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ChoiceButton(
                        text = "Кондиция",
                        selected = !isNonConformity,
                        selectedColor = GoodColor,
                        onClick = { condition = "Кондиция"; errorMessage = null },
                        modifier = Modifier.weight(1f)
                    )
                    ChoiceButton(
                        text = "Некондиция",
                        selected = isNonConformity,
                        selectedColor = BadColor,
                        onClick = { condition = "Некондиция"; errorMessage = null },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isNonConformity) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionTitle("Причина некондиции")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 0.dp,
                        backgroundColor = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column {
                            ConditionReasons.reasons.forEach { item ->
                                val selected = reason == item
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { reason = item; errorMessage = null }
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = selected, onClick = { reason = item; errorMessage = null })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item,
                                        fontSize = 15.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SectionTitle("Срок годности")
                OutlinedButton(
                    onClick = { skipExpiration = !skipExpiration; errorMessage = null },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        if (skipExpiration) "✓ Срок не указан (2999)" else "Срок не указан — нажмите, если нет даты",
                        fontSize = 14.sp,
                        fontWeight = if (skipExpiration) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (!skipExpiration) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startDateField,
                        onValueChange = { typed ->
                            val formatted = formatDateInput(typed.text)
                            startDate = formatted
                            startDateField = TextFieldValue(formatted, TextRange(formatted.length))
                            errorMessage = null
                        },
                        label = { Text("Дата производства", fontSize = 13.sp) },
                        placeholder = { Text("дд.мм.гггг", fontSize = 16.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = days,
                            onValueChange = { if (it.all(Char::isDigit) && it.length <= 4) { days = it; errorMessage = null } },
                            label = { Text("Дни", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = months,
                            onValueChange = { if (it.all(Char::isDigit) && it.length <= 3) { months = it; errorMessage = null } },
                            label = { Text("Месяцы", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 0.dp,
                        backgroundColor = if (endDate.isNotEmpty()) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Годен до", fontSize = 13.sp)
                            Text(
                                text = endDate.ifEmpty { "—" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (endDate.isNotEmpty()) GoodColor else Color.Gray
                            )
                        }
                    }
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, fontSize = 14.sp, color = MaterialTheme.colors.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val error = viewModel.save(
                        skipExpiration = skipExpiration,
                        startDate = startDate,
                        days = days,
                        months = months,
                        condition = condition,
                        reason = reason
                    )
                    if (error == null) {
                        navController.navigate("scan_buffer_location") {
                            popUpTo("expiration_date") { inclusive = true }
                        }
                    } else {
                        errorMessage = error
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                )
            ) {
                Text("СОХРАНИТЬ И ДАЛЕЕ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp, top = 4.dp)
    )
}

@Composable
private fun ChoiceButton(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (selected) selectedColor else Color(0xFFE0E0E0),
            contentColor = if (selected) Color.White else Color.Black
        )
    ) {
        Text(
            text = if (selected) "✓ $text" else text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
