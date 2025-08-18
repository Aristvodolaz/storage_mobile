package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.komus.sorage_mobile.util.SPHelper
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.domain.state.PlacementState
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import com.komus.sorage_mobile.domain.viewModel.PlacementViewModel
import com.komus.sorage_mobile.util.Screen

@Composable
fun ProductInfoScreen(
    navController: NavController,
    spHelper: SPHelper,
    placementViewModel: PlacementViewModel = hiltViewModel()
) {
    val productId = spHelper.getProductId()
    val brief = spHelper.getTypeBrief()
    val fullQnt = spHelper.getFullQnt()
    val srokGodnosti = spHelper.getSrokGodnosti()
    val condition = spHelper.getCondition()
    val scrollState = rememberScrollState()
    val placementState by placementViewModel.state.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Обработка состояния размещения
    LaunchedEffect(placementState) {
        when (placementState) {
            is PlacementState.Loading -> {
                isLoading = true
            }
            is PlacementState.Success -> {
                isLoading = false
                successMessage = (placementState as PlacementState.Success).message
                showSuccessDialog = true
            }
            is PlacementState.Error -> {
                isLoading = false
                errorMessage = (placementState as PlacementState.Error).message
                showErrorDialog = true
            }
            else -> {
                isLoading = false
            }
        }
    }

    // Диалог успешного размещения
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                placementViewModel.resetState()
                navController.popBackStack("product_info", inclusive = true)
                navController.navigate(Screen.Placement.route)
            },
            title = { Text("Успешно!") },
            text = { 
                Column {
                    Text(successMessage)
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        placementViewModel.resetState()
                        navController.popBackStack("product_info", inclusive = true)
                        navController.navigate(Screen.Placement.route)
                    }
                ) {
                    Text("OK")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }

    // Диалог ошибки
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                placementViewModel.resetState()
            },
            title = { Text("Ошибка") },
            text = { 
                Column {
                    Text(errorMessage)
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        placementViewModel.resetState()
                    }
                ) {
                    Text("OK")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Информация о товаре", 
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Артикул:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = productId,
                        style = MaterialTheme.typography.body1
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Единица хранения:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = brief,
                        style = MaterialTheme.typography.body1
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Количество:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$fullQnt шт.",
                        style = MaterialTheme.typography.body1
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Срок годности:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = srokGodnosti,
                        style = MaterialTheme.typography.body1
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Состояние:",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = condition,
                        style = MaterialTheme.typography.body1,
                        color = if (condition == "Кондиция") Color(0xFF4CAF50) else Color(0xFFFF5722)
                    )
                }
            }
        }


    }
}



@Composable
fun ScanBufferLocationScreen(
    navController: NavController,
    spHelper: SPHelper,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    placementViewModel: PlacementViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var locationId by remember { mutableStateOf("") }
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val placementState by placementViewModel.state.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var manualInput by remember { mutableStateOf("") }
    val isCipherLabDevice = remember { Build.MANUFACTURER.contains("cipherlab", ignoreCase = true) }
    val manualInputFocusRequester = remember { FocusRequester() }

    // Обработка состояния размещения
    LaunchedEffect(placementState) {
        when (placementState) {
            is PlacementState.Loading -> {
                isLoading = true
            }
            is PlacementState.Success -> {
                isLoading = false
                successMessage = (placementState as PlacementState.Success).message
                showSuccessDialog = true
            }
            is PlacementState.Error -> {
                isLoading = false
                errorMessage = (placementState as PlacementState.Error).message
                showErrorDialog = true
            }
            else -> {
                isLoading = false
            }
        }
    }
    LaunchedEffect(manualInput) {
        if (isCipherLabDevice && manualInput.length in 3..64) {
            locationId = manualInput
            manualInput = ""

            Log.d("ScanBufferLocationScreen", "Введён ШК вручную: $locationId")

            // Автоматически устанавливаем состояние "Кондиция"
            spHelper.saveCondition("Кондиция")
            spHelper.saveReason("")

            spHelper.saveBufferLocation(locationId)
            spHelper.saveWrShk(locationId)
            spHelper.saveSkladId("1383")

            val productId = spHelper.getProductId()
            val brief = spHelper.getBrief()
            val fullQnt = spHelper.getFullQnt()

            Log.d("ScanBufferLocationScreen", "Отправка запроса на размещение (ручной ввод): productId=$productId, prunitId=$brief, quantity=$fullQnt")

            placementViewModel.placeProductToBuffer(
                productId = productId,
                prunitId = brief,
                quantity = fullQnt
            )
        }
    }

    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            locationId = barcodeData
            scannerViewModel.clearBarcode()

            Log.d("ScanBufferLocationScreen", "Получен штрих-код: $locationId")

            // Автоматически устанавливаем состояние "Кондиция"
            spHelper.saveCondition("Кондиция")
            spHelper.saveReason("")

            // Сохраняем ячейку буфера
            spHelper.saveBufferLocation(locationId)
            spHelper.saveWrShk(locationId)
            spHelper.saveSkladId("1383")

            val productId = spHelper.getProductId()
            val brief = spHelper.getBrief()
            val fullQnt = spHelper.getFullQnt()

            Log.d("ScanBufferLocationScreen", "Отправка запроса на размещение: productId=$productId, prunitId=$brief, quantity=$fullQnt")

            placementViewModel.placeProductToBuffer(
                productId = productId,
                prunitId = brief,
                quantity = fullQnt
            )
        }
    }

    // Диалог успешного размещения
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                placementViewModel.resetState()
                navController.popBackStack("scan_buffer_location", inclusive = true)
                navController.navigate(Screen.Placement.route)
            },
            title = { Text("Успешно!") },
            text = { 
                Column {
                    Text(successMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ячейка буфера: $locationId",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        placementViewModel.resetState()
                        navController.popBackStack("scan_buffer_location", inclusive = true)
                        navController.navigate(Screen.Placement.route)
                    }
                ) {
                    Text("OK")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }
    
    // Диалог ошибки
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                placementViewModel.resetState()
            },
            title = { Text("Ошибка") },
            text = { 
                Column {
                    Text(errorMessage)
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        placementViewModel.resetState()
                    }
                ) {
                    Text("OK")
                }
            },
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.onSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Сканирование ячейки буфера",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Для подтверждения отсканируйте ШК ячейки буфера",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isCipherLabDevice) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        label = { Text("ШК ячейки буфера (ввод)") },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .focusRequester(manualInputFocusRequester),
                        singleLine = true
                    )
                    
                    // Автофокус на поле ввода при показе
                    LaunchedEffect(isCipherLabDevice) {
                        if (isCipherLabDevice) {
                            kotlinx.coroutines.delay(100) // Небольшая задержка для инициализации UI
                            manualInputFocusRequester.requestFocus()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (locationId.isNotEmpty()) {
                    Text(
                        text = "Ячейка буфера: $locationId",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colors.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
            }
        }
    }
}
