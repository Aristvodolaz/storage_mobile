package com.komus.sorage_mobile.presentation.screens.snyatie

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.state.LocationItemsState
import com.komus.sorage_mobile.domain.viewModel.PickViewModel

@Composable
fun ItemSelectionScreen(
    navController: NavController,
    pickViewModel: PickViewModel = hiltViewModel()
) {
    val locationItemsState by pickViewModel.locationItemsState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Выберите товар", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        when (locationItemsState) {
            is LocationItemsState.Success -> {
                val items = (locationItemsState as LocationItemsState.Success).items
                if (items.isEmpty()) {
                    Text(
                        text = "Ячейка пуста",
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    LazyColumn {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                onClick = {
                                    pickViewModel.selectItem(item)
                                    navController.navigate("pick_quantity")
                                }
                            )
                        }
                    }
                }
            }
            is LocationItemsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is LocationItemsState.Error -> {
                val errorMessage = (locationItemsState as LocationItemsState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body1
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Назад")
                }
            }
            else -> {
                // Если состояние Initial, возвращаемся назад
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: LocationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Артикул: ${item.article}",
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Количество: ${item.quantity}",
                style = MaterialTheme.typography.body2
            )
            if (item.barcode != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Штрихкод: ${item.barcode}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
} 