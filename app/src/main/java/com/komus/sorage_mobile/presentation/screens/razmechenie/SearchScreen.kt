package com.komus.sorage_mobile.presentation.screens.razmechenie

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.model.ConditionReasons
import com.komus.sorage_mobile.data.response.SearchItem
import com.komus.sorage_mobile.domain.state.SearchState
import com.komus.sorage_mobile.domain.viewModel.PlacementFlowViewModel
import com.komus.sorage_mobile.domain.viewModel.SearchViewModel
import com.komus.sorage_mobile.util.SPHelper

/** Штрихкод — только цифры и не короче 8 символов; всё остальное считаем артикулом. */
internal fun isBarcodeQuery(query: String): Boolean =
    query.length >= 8 && query.all { it.isDigit() }

/**
 * Поиск товара для размещения.
 * Скан запускает поиск сразу, без ручного нажатия. Один найденный товар открывается автоматически,
 * заблокированный не открывается никогда. Остальные показываются списком с крупными строками для ТСД.
 */
@Composable
fun SearchScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    spHelper: SPHelper,
    placementFlow: PlacementFlowViewModel = hiltViewModel(),
    onScanClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()
    var navigating by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    fun runSearch(text: String) {
        val value = text.trim()
        if (value.isEmpty()) return
        navigating = false
        val isShk = isBarcodeQuery(value)
        searchViewModel.search(shk = if (isShk) value else null, article = if (!isShk) value else null)
    }

    // Сброс при входе и фокус на поле ввода
    LaunchedEffect(Unit) {
        searchViewModel.resetState()
        navigating = false
        kotlinx.coroutines.delay(100)
        searchFocusRequester.requestFocus()
    }

    // Скан: подставляем код и сразу ищем
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            query = barcodeData.trim()
            scannerViewModel.clearBarcode()
            runSearch(query)
        }
    }

    // Один подходящий товар открываем сами; заблокированный — только показываем
    LaunchedEffect(searchState) {
        val state = searchState
        if (state is SearchState.Success && state.data.size == 1 && !navigating) {
            val item = state.data.first()
            if (!isBlocked(item)) {
                navigating = true
                placementFlow.update { draftWithProduct(it, item) }
                searchViewModel.resetState()
                navController.navigate("search_results/${item.ID}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Поиск товара", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            // Поле ввода: крупный шрифт, очистка одним касанием
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Артикул или штрихкод", fontSize = 13.sp) },
                placeholder = { Text("Отсканируйте или введите", fontSize = 16.sp) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                keyboardActions = KeyboardActions(onSearch = { runSearch(query) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { runSearch(query) },
                enabled = query.isNotBlank() && searchState !is SearchState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                )
            ) {
                Text("НАЙТИ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = searchState) {
                is SearchState.Idle -> StatusCard(
                    text = "Отсканируйте штрихкод товара или введите артикул",
                    background = Color(0xFFF5F5F5),
                    textColor = Color.DarkGray
                )
                is SearchState.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Поиск товара…", fontSize = 14.sp)
                }
                is SearchState.Error -> StatusCard(
                    text = state.message,
                    background = Color(0xFFFFEBEE),
                    textColor = MaterialTheme.colors.error
                )
                is SearchState.Success -> {
                    if (state.data.size > 1) {
                        Text(
                            text = "Найдено: ${state.data.size}. Выберите товар",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.data) { item ->
                                SearchResultCard(
                                    item = item,
                                    onClick = {
                                        placementFlow.update { draftWithProduct(it, item) }
                                        searchViewModel.resetState()
                                        navController.navigate("search_results/${item.ID}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isBlocked(item: SearchItem) = item.REASON == ConditionReasons.BLOCKED

private fun draftWithProduct(
    draft: com.komus.sorage_mobile.data.placement.PlacementDraft,
    item: SearchItem
) = draft.copy(
    article = item.ARTICLE_ID_REAL,
    shk = item.SHK ?: "",
    productName = item.NAME
)

@Composable
private fun StatusCard(text: String, background: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        backgroundColor = background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }
}

@Composable
private fun SearchResultCard(item: SearchItem, onClick: () -> Unit) {
    val blocked = isBlocked(item)
    val uncertain = !blocked && item.CONDITION_STATE == "BAD"

    val background = when {
        blocked -> Color(0xFFFFEBEE)
        uncertain -> Color(0xFFFFF8E1)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !blocked, onClick = onClick),
        elevation = if (blocked) 0.dp else 2.dp,
        backgroundColor = background,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.NAME,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Арт. ${item.ARTICLE_ID_REAL}" + (item.SHK?.takeIf { it.isNotEmpty() }?.let { "  ШК $it" } ?: ""),
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "В паллете: ${item.QNT_IN_PALLET}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (blocked) {
                    Text(
                        text = "Заблокирован: нельзя разместить",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.error
                    )
                } else if (uncertain) {
                    Text(
                        text = "Требует проверки состояния",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57C00)
                    )
                }
            }
            if (!blocked) {
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
