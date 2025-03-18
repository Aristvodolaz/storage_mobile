package com.komus.sorage_mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.sorage_mobile.domain.viewModel.AppViewModel

@Composable
fun TopConnectionStatusBar(
    appViewModel: AppViewModel = hiltViewModel()
) {
    val isConnected by appViewModel.isConnected.collectAsState()
    val currentWarehouse by appViewModel.currentWarehouse.collectAsState()

    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp,
        modifier = Modifier.height(64.dp) // Увеличиваем высоту тулбара
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Добавляем вертикальные отступы
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