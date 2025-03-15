package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.response.SearchItem
import com.komus.sorage_mobile.domain.state.SearchState
import com.komus.sorage_mobile.domain.viewModel.SearchViewModel
import com.komus.sorage_mobile.util.SPHelper

@Composable
fun SearchScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    spHelper: SPHelper = hiltViewModel(),
    onScanClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }  // Универсальное поле для поиска
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()
    var selectedItem by remember { mutableStateOf<SearchItem?>(null) }
    var navigating by remember { mutableStateOf(false) }
    
    // Сбрасываем состояние при входе на экран
    LaunchedEffect(Unit) {
        searchViewModel.resetState()
        navigating = false
    }

    // Автопоиск при сканировании
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("SearchScreen", "Searching barcode: $barcodeData")
            query = barcodeData
            scannerViewModel.clearBarcode()
        }
    }
    
    // Обработка навигации при получении одного результата
    LaunchedEffect(searchState) {
        if (searchState is SearchState.Success && !navigating) {
            val results = (searchState as SearchState.Success).data
            if (results.size == 1) {
                navigating = true
                selectedItem = results.first()
                
                // Сохраняем дополнительные данные о товаре
                saveProductData(spHelper, selectedItem!!)
                
                navController.navigate("search_results/${selectedItem!!.ID}")
                // Сбрасываем состояние после навигации
                searchViewModel.resetState()
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Поиск товара", style = MaterialTheme.typography.h6)

        // Поле ввода (артикул или ШК)
        TextField(
            value = query,
            onValueChange = { newValue ->
                query = newValue.trim()
            },
            label = { Text("Введите артикул или ШК") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка поиска
        Button(
            onClick = {
                if (query.isNotEmpty()) {
                    val isShk = query.length > 7
                    navigating = false // Сбрасываем флаг навигации перед новым поиском
                    searchViewModel.search(shk = if (isShk) query else null, article = if (!isShk) query else null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Поиск")
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (searchState) {
            is SearchState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            is SearchState.Success -> {
                val results = (searchState as SearchState.Success).data
                if (results.size > 1) {
                    Column {
                        Text("Выберите один товар:", style = MaterialTheme.typography.body1)
                        results.forEach { item ->
                            Button(
                                onClick = {
                                    selectedItem = item
                                    navigating = true
                                    
                                    // Сохраняем дополнительные данные о товаре
                                    saveProductData(spHelper, item)
                                    
                                    navController.navigate("search_results/${selectedItem!!.ID}")
                                    // Сбрасываем состояние после навигации
                                    searchViewModel.resetState()
                                },
                                modifier = Modifier.fillMaxWidth().padding(4.dp)
                            ) {
                                Text(text = item.NAME)
                            }
                        }
                    }
                } else if (results.size == 1 && !navigating) {
                    // Показываем информацию о переходе
                    Text("Найден один товар, переход на страницу товара...")
                }
            }
            is SearchState.Error -> {
                val errorMessage = (searchState as SearchState.Error).message
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

// Функция для сохранения данных о товаре в SPHelper
private fun saveProductData(spHelper: SPHelper, item: SearchItem) {
    // Сохраняем артикул
    spHelper.saveArticle(item.ARTICLE_ID_REAL)
    
    // Сохраняем штрихкод
    spHelper.saveShk(item.SHK)
    
    // Сохраняем название товара
    spHelper.saveProductName(item.NAME)
}