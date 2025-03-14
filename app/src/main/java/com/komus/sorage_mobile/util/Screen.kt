package com.komus.sorage_mobile.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Inventory : Screen("inventory", "Инвентаризация", Icons.Filled.List)
    object Placement : Screen("placement", "Размещение", Icons.Filled.Add)
    object Removal : Screen("removal", "Снятие", Icons.Filled.Delete)
    object Movement : Screen("movement", "Перемещение", Icons.Filled.SwapHoriz)
    object Info : Screen("info", "Поиск", Icons.Filled.Search)
}