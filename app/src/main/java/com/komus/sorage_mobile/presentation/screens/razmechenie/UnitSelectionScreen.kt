package com.komus.sorage_mobile.presentation.screens.razmechenie

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import com.komus.sorage_mobile.data.response.UnitItem
import com.komus.sorage_mobile.domain.state.UnitState
import com.komus.sorage_mobile.domain.viewModel.PlacementFlowViewModel
import com.komus.sorage_mobile.domain.viewModel.UnitViewModel
import com.komus.sorage_mobile.presentation.components.PlacementProductHeader
import com.komus.sorage_mobile.presentation.screens.movement.ExPalletType
import com.komus.sorage_mobile.util.SPHelper

/** ЕХ-паллет определяется по названию единицы. */
private fun isExUnit(unit: UnitItem?): Boolean =
    unit?.typeName?.let { it.contains("ЕХ", ignoreCase = true) || it.contains("EX", ignoreCase = true) } ?: false

/**
 * Выбор единицы хранения и количества.
 * Шаг 1 — крупный список единиц; шаг 2 — количество упаковок с итогом в штуках и типом ЕХ-паллета.
 * Кнопка «ДАЛЕЕ» активна только при количестве больше нуля.
 */
@Composable
fun UnitSelectionScreen(
    navController: NavController,
    productId: String,
    spHelper: SPHelper,
    unitViewModel: UnitViewModel = hiltViewModel(),
    placementFlow: PlacementFlowViewModel = hiltViewModel()
) {
    val unitState by unitViewModel.unitState.collectAsState()
    var selectedUnit by remember { mutableStateOf<UnitItem?>(null) }
    var quantity by remember { mutableStateOf("") }
    var showQuantityInput by remember { mutableStateOf(false) }
    var selectedExPalletType by remember { mutableStateOf(ExPalletType.EURO) }
    val quantityFocusRequester = remember { FocusRequester() }

    val parsedQuantity = quantity.toIntOrNull() ?: 0
    val unitsPerPack = selectedUnit?.productQnt ?: 1

    LaunchedEffect(productId) {
        unitViewModel.fetchUnits(productId)
    }

    // Фокус на поле количества сразу после выбора единицы
    LaunchedEffect(showQuantityInput) {
        if (showQuantityInput) {
            kotlinx.coroutines.delay(100)
            quantityFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showQuantityInput) "Количество" else "Единица хранения",
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
            Spacer(modifier = Modifier.height(8.dp))

            when (val state = unitState) {
                is UnitState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is UnitState.Success -> {
                    if (!showQuantityInput) {
                        UnitList(
                            units = state.data,
                            onSelect = { unit ->
                                selectedUnit = unit
                                quantity = ""
                                showQuantityInput = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        QuantityStep(
                            unit = selectedUnit,
                            quantity = quantity,
                            onQuantityChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 6) quantity = value
                            },
                            isEx = isExUnit(selectedUnit),
                            selectedExPalletType = selectedExPalletType,
                            onExPalletSelected = { selectedExPalletType = it },
                            quantityFocusRequester = quantityFocusRequester,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showQuantityInput = false },
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) {
                                Text("НАЗАД", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val unit = selectedUnit ?: return@Button
                                    placementFlow.update {
                                        it.copy(
                                            brief = unit.type.toString(),
                                            typeBrief = unit.typeName,
                                            productId = productId,
                                            productQnt = unit.productQnt,
                                            fullQnt = parsedQuantity * unit.productQnt,
                                            exPalletType = if (isExUnit(unit)) selectedExPalletType.name else ""
                                        )
                                    }
                                    navController.navigate("expiration_date")
                                },
                                enabled = parsedQuantity > 0,
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("ДАЛЕЕ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is UnitState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 0.dp,
                        backgroundColor = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Не удалось загрузить единицы", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.message, fontSize = 13.sp, color = MaterialTheme.colors.error, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { unitViewModel.fetchUnits(productId) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("ПОВТОРИТЬ", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun UnitList(
    units: List<UnitItem>,
    onSelect: (UnitItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Выберите упаковку",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
    if (units.isEmpty()) {
        Text("Для товара нет единиц хранения", fontSize = 15.sp, color = Color.Gray)
        return
    }
    LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(units) { unit ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(unit) },
                elevation = 2.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = unit.typeName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "В упаковке: ${unit.productQnt} шт.", fontSize = 13.sp, color = Color.DarkGray)
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Выбрать",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStep(
    unit: UnitItem?,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    isEx: Boolean,
    selectedExPalletType: ExPalletType,
    onExPalletSelected: (ExPalletType) -> Unit,
    quantityFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val perPack = unit?.productQnt ?: 1
    val count = quantity.toIntOrNull() ?: 0
    Column(modifier = modifier.fillMaxWidth()) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = 0.dp, backgroundColor = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = unit?.typeName ?: "", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "В упаковке: $perPack шт.", fontSize = 13.sp, color = Color.DarkGray)
            }
        }

        if (isEx) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Тип ЕХ-паллета", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            ExPalletTypeSelector(selected = selectedExPalletType, onSelected = onExPalletSelected)
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Количество упаковок", fontSize = 13.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(quantityFocusRequester)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 0.dp, backgroundColor = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Итого к размещению", fontSize = 13.sp)
                Text(
                    text = if (count > 0) "${count * perPack} шт." else "—",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun ExPalletTypeSelector(
    selected: ExPalletType,
    onSelected: (ExPalletType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ExPalletType.values().forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(type) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected == type, onClick = { onSelected(type) })
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = type.displayName,
                        fontSize = 15.sp,
                        fontWeight = if (selected == type) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "${type.description} · адресов: ${type.addressesCount}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
