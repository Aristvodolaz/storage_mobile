package com.komus.sorage_mobile.presentation.screens.search

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.response.LocationDetails
import com.komus.sorage_mobile.data.response.ProductItem
import com.komus.sorage_mobile.data.response.UnitWithLocations
import com.komus.sorage_mobile.domain.state.ProductSearchState
import com.komus.sorage_mobile.domain.viewModel.ProductSearchViewModel

@Composable
fun ProductSearchScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    productSearchViewModel: ProductSearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val searchState by productSearchViewModel.searchState.collectAsStateWithLifecycle()
    
    // Очищаем состояние при входе на экран
    LaunchedEffect(Unit) {
        productSearchViewModel.resetSearchState()
    }
    
    // Автозаполнение при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("ProductSearchScreen", "Сканирован код: $barcodeData")
            searchQuery = barcodeData
            scannerViewModel.clearBarcode()
            
            // Выполняем поиск
            productSearchViewModel.searchProducts(searchQuery)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Поиск товаров", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле поиска
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.trim() },
                label = { Text("Артикул или штрихкод") },
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        productSearchViewModel.searchProducts(searchQuery)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                if (searchQuery.isNotEmpty()) {
                    productSearchViewModel.searchProducts(searchQuery)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotEmpty()
        ) {
            Text("Поиск")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Отображение результатов поиска
        when (searchState) {
            is ProductSearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProductSearchState.Success -> {
                val products = (searchState as ProductSearchState.Success).products
                LazyColumn {
                    items(products) { product ->
                        ProductCard(product = product)
                    }
                }
            }
            is ProductSearchState.Error -> {
                val errorMessage = (searchState as ProductSearchState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body1
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ProductCard(product: ProductItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.h6
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ID: ${product.id}",
                style = MaterialTheme.typography.body2
            )
            
            Text(
                text = "Артикул: ${product.article}",
                style = MaterialTheme.typography.body2
            )
            
            Text(
                text = "Штрихкод: ${product.shk}",
                style = MaterialTheme.typography.body2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Единицы хранения:",
                style = MaterialTheme.typography.subtitle1
            )
            
            product.units.forEach { (unitKey, unit) ->
                UnitCard(unitKey = unitKey, unit = unit)
            }
        }
    }
}

@Composable
fun UnitCard(unitKey: String, unit: UnitWithLocations) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "${unit.name} (${unitKey})",
                style = MaterialTheme.typography.subtitle2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Ячейки:",
                style = MaterialTheme.typography.body2
            )
            
            unit.locations.forEach { location ->
                LocationCard(location = location)
            }
        }
    }
}

@Composable
fun LocationCard(location: LocationDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "${location.name} (${location.wrShk})",
                style = MaterialTheme.typography.body2
            )
            
            Row {
                Text(
                    text = "Количество: ${location.quantity}",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "Состояние: ${location.conditionState}",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Text(
                text = "Срок годности: ${location.expirationDate}",
                style = MaterialTheme.typography.caption
            )
        }
    }
} 