package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
    spHelper: SPHelper,
    onScanClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Поиск товара",
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                modifier = Modifier.height(44.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Поле ввода и кнопка сканирования
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Введите артикул или отсканируйте ШК",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { newValue -> query = newValue.trim() },
                            label = { 
                                Text(
                                    "Артикул/ШК",
                                    fontSize = 10.sp
                                )
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Поиск",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                    
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = {
                            if (query.isNotEmpty()) {
                                val isShk = query.length > 7
                                navigating = false
                                searchViewModel.search(shk = if (isShk) query else null, article = if (!isShk) query else null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        enabled = query.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "ПОИСК",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            when (searchState) {
                is SearchState.Loading -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Поиск товара...",
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                is SearchState.Success -> {
                    val results = (searchState as SearchState.Success).data
                    if (results.size > 1) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(4.dp),
                            backgroundColor = Color(0xFFE8F5E9)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    "Найдено товаров: ${results.size}",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    "Выберите товар из списка:",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                results.forEach { item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clickable {
                                                selectedItem = item
                                                navigating = true
                                                saveProductData(spHelper, item)
                                                navController.navigate("search_results/${selectedItem!!.ID}")
                                                searchViewModel.resetState()
                                            },
                                        elevation = 1.dp,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp)
                                        ) {
                                            Text(
                                                text = item.NAME,
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            
                                            Spacer(modifier = Modifier.height(2.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Арт: ${item.ARTICLE_ID_REAL}",
                                                    style = MaterialTheme.typography.caption,
                                                    fontSize = 8.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                
                                                if (item.SHK.isNotEmpty()) {
                                                    Text(
                                                        text = "ШК: ${item.SHK}",
                                                        style = MaterialTheme.typography.caption,
                                                        fontSize = 8.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (results.size == 1 && !navigating) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(4.dp),
                            backgroundColor = Color(0xFFE8F5E9)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Найден товар, переход...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
                is SearchState.Error -> {
                    val errorMessage = (searchState as SearchState.Error).message
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(4.dp),
                        backgroundColor = Color(0xFFFFEBEE)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Ошибка поиска",
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colors.error
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                color = MaterialTheme.colors.error
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ScanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.primary)
            .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Сканировать",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
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