package com.komus.sorage_mobile.presentation.screens.razmechenie

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp import androidx.navigation.NavController
import com.komus.sorage_mobile.util.SPHelper

@Composable
fun ProductInfoScreen(
    navController: NavController,
    spHelper: SPHelper
) {
    val productId = spHelper.getProductId()
    val brief = spHelper.getBrief()
    val fullQnt = spHelper.getFullQnt()
    val srokGodnosti = spHelper.getSrokGodnosti()
    val condition = spHelper.getCondition()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Информация о товаре", style = MaterialTheme.typography.h6)

        Text(text = "Артикул: $productId", style = MaterialTheme.typography.body1)
        Text(text = "EX: $brief", style = MaterialTheme.typography.body1)
        Text(text = "Кол-во: $fullQnt", style = MaterialTheme.typography.body1)
        Text(text = "Срок годности: $srokGodnosti", style = MaterialTheme.typography.body1)
        Text(text = "Состояние: $condition", style = MaterialTheme.typography.body1)

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("scan_storage_location")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun ScanLocationScreen(navController: NavController,
                      spHelper: SPHelper) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Для подтверждения отсканируйте ШК ячейки размещения", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // TODO: Добавить логику сканирования
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Сканировать")
        }
    }
}
