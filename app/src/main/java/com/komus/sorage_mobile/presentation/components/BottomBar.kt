package com.komus.sorage_mobile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.sorage_mobile.domain.viewModel.AppViewModel

@Composable
fun BottomConnectionStatusBar(
    appViewModel: AppViewModel = hiltViewModel()
) {
    val isConnected by appViewModel.isConnected.collectAsState()
    val currentWarehouse by appViewModel.currentWarehouse.collectAsState()

    BottomAppBar(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор подключения
            ConnectionStatusIndicator(isConnected = isConnected)

            // Выбор склада
            WarehouseSelector(
                currentWarehouse = currentWarehouse,
                onWarehouseSelected = { warehouseId ->
                    appViewModel.setWarehouse(warehouseId)
                }
            )
        }
    }
} 