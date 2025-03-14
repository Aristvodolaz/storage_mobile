package com.komus.sorage_mobile.presentation.screens.razmechenie

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    LaunchedEffect(productId) {
        unitViewModel.fetchUnits(productId)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Выберите единицу хранения", style = MaterialTheme.typography.h6)

        when (unitState) {
            is UnitState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            is UnitState.Success -> {
                val units = (unitState as UnitState.Success).data
                Column {
                    units.forEach { unit ->
                        Button(
                            onClick = { selectedUnit = unit },
                            modifier = Modifier.fillMaxWidth().padding(4.dp)
                        ) {
                            Text(text = "${unit.typeName} (${unit.productQnt})")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Введите количество") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedUnit?.let {
                                if(quantity.isNotEmpty()) {
                                    val fullQnt = quantity.toInt() * selectedUnit!!.productQnt
                                    spHelper.saveFullQnt(fullQnt)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedUnit != null && quantity.isNotEmpty()
                    ) {
                        Text("Подтвердить выбор")
                    }
                }
            }
            is UnitState.Error -> {
                Text(text = "Ошибка: ${(unitState as UnitState.Error).message}", color = MaterialTheme.colors.error)
            }
            else -> {}
        }
    }
}
