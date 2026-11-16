package com.komus.sorage_mobile.presentation.screens.razmechenie

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.data.placement.PlacementDraft
import com.komus.sorage_mobile.domain.state.PlacementState
import com.komus.sorage_mobile.domain.viewModel.PlacementFlowViewModel
import com.komus.sorage_mobile.domain.viewModel.PlacementViewModel
import com.komus.sorage_mobile.presentation.components.PlacementProductHeader
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.util.Screen

private val OkColor = Color(0xFF2E7D32)
private val WarnColor = Color(0xFFE65100)

/** ISO yyyy-MM-dd -> dd.MM.yyyy для показа оператору. */
internal fun isoToDisplayDate(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3 && parts[0].length == 4) "${parts[2]}.${parts[1]}.${parts[0]}" else iso
}

/**
 * Выбор ячейки буфера и подтверждение размещения.
 * Ячейка принимается только если сервер подтвердил её существование. Если связи нет и ячейку
 * проверить нельзя, размещение уходит в очередь с пометкой. Несуществующую ячейку подтвердить нельзя.
 */
@Composable
fun ScanBufferLocationScreen(
    navController: NavController,
    spHelper: SPHelper,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    placementViewModel: PlacementViewModel = hiltViewModel(),
    placementFlow: PlacementFlowViewModel = hiltViewModel()
) {
    val draft by placementFlow.draft.collectAsStateWithLifecycle()
    val check by placementViewModel.locationCheck.collectAsStateWithLifecycle()
    val placementState by placementViewModel.state.collectAsStateWithLifecycle()
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()

    var pendingLocationId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showWarningsDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var warningMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var manualInput by remember { mutableStateOf("") }
    val isCipherLabDevice = remember { Build.MANUFACTURER.contains("cipherlab", ignoreCase = true) }
    val manualInputFocusRequester = remember { FocusRequester() }

    fun handleScannedLocation(scanned: String) {
        val code = scanned.trim()
        if (code.isEmpty()) return
        pendingLocationId = code
        placementViewModel.fetchLocationName(code)
    }

    fun cancelLocation() {
        pendingLocationId = ""
        placementViewModel.clearLocationName()
    }

    fun navigateAfterSuccess() {
        placementViewModel.resetState()
        // Размещение подтверждено: следующий товар начинается с чистого черновика
        placementFlow.clear()
        placementViewModel.clearLocationName()
        navController.popBackStack("scan_buffer_location", inclusive = true)
        navController.navigate(Screen.Placement.route)
    }

    fun confirmPlacement() {
        placementFlow.update { it.copy(bufferLocation = pendingLocationId, wrShk = pendingLocationId) }
        val current = placementFlow.draft.value
        Log.d("ScanBufferLocationScreen", "Подтверждение размещения: productId=${current.productId}, quantity=${current.fullQnt}")
        placementViewModel.placeProductToBuffer(
            productId = current.productId,
            prunitId = current.brief,
            quantity = current.fullQnt
        )
    }

    // Результат размещения
    LaunchedEffect(placementState) {
        when (val state = placementState) {
            is PlacementState.Loading -> isLoading = true
            is PlacementState.Success -> {
                isLoading = false
                successMessage = state.message
                if (state.warnings.isNotEmpty()) {
                    warningMessages = state.warnings
                    showWarningsDialog = true
                } else {
                    showSuccessDialog = true
                }
            }
            is PlacementState.Error -> {
                isLoading = false
                errorMessage = state.message
                showErrorDialog = true
            }
            else -> isLoading = false
        }
    }

    // Сканер: код ячейки
    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            val scanned = barcodeData
            scannerViewModel.clearBarcode()
            handleScannedLocation(scanned)
        }
    }

    // CipherLab: ввод с клавиатуры
    LaunchedEffect(manualInput) {
        if (isCipherLabDevice && manualInput.length in 3..64) {
            val scanned = manualInput
            manualInput = ""
            handleScannedLocation(scanned)
        }
    }

    LaunchedEffect(isCipherLabDevice) {
        if (isCipherLabDevice) {
            kotlinx.coroutines.delay(100)
            manualInputFocusRequester.requestFocus()
        }
    }

    if (showWarningsDialog) {
        AlertDialog(
            onDismissRequest = { showWarningsDialog = false; navigateAfterSuccess() },
            title = { Text("Внимание") },
            text = {
                Column {
                    Text(successMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    warningMessages.forEach { msg ->
                        Text("⚠ $msg", color = WarnColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showWarningsDialog = false; navigateAfterSuccess() }) { Text("OK") }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; navigateAfterSuccess() },
            title = { Text("Размещено") },
            text = {
                Column {
                    Text(successMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ячейка: $pendingLocationId", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
                }
            },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; navigateAfterSuccess() }) { Text("OK") }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false; placementViewModel.resetState() },
            title = { Text("Не размещено") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false; placementViewModel.resetState() }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        PlacementProductHeader()
        Spacer(modifier = Modifier.height(8.dp))
        PlacementSummary(draft)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            elevation = 0.dp,
            backgroundColor = Color(0xFFF5F5F5),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (pendingLocationId.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Отсканируйте ячейку буфера",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Размещение подтвердится после проверки ячейки",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    if (isCipherLabDevice) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = { manualInput = it },
                            label = { Text("Код ячейки") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().focusRequester(manualInputFocusRequester)
                        )
                    }
                } else {
                    Text("Ячейка", fontSize = 14.sp, color = Color.DarkGray)
                    Text(
                        text = pendingLocationId,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LocationStatus(check)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (pendingLocationId.isNotEmpty()) {
            val canConfirm = !isLoading && (check is PlacementViewModel.LocationCheck.Found ||
                check is PlacementViewModel.LocationCheck.Unverified)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { cancelLocation() },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("ДРУГАЯ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { confirmPlacement() },
                    enabled = canConfirm,
                    modifier = Modifier.weight(2f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ПОДТВЕРДИТЬ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LocationStatus(check: PlacementViewModel.LocationCheck) {
    when (check) {
        is PlacementViewModel.LocationCheck.Checking -> Text("Проверка ячейки…", fontSize = 15.sp, color = Color.DarkGray)
        is PlacementViewModel.LocationCheck.Found -> Text(
            "✓ ${check.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OkColor, textAlign = TextAlign.Center
        )
        is PlacementViewModel.LocationCheck.NotFound -> Text(
            "Ячейка не найдена в системе.\nОтсканируйте другую.",
            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.error, textAlign = TextAlign.Center
        )
        is PlacementViewModel.LocationCheck.CheckFailed -> Text(
            "Сервер не смог проверить ячейку.\nОтсканируйте её ещё раз.",
            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.error, textAlign = TextAlign.Center
        )
        is PlacementViewModel.LocationCheck.Unverified -> Text(
            "Нет связи: ячейку не проверить.\nРазмещение сохранится и отправится позже.",
            fontSize = 14.sp, color = WarnColor, textAlign = TextAlign.Center
        )
        else -> Unit
    }
}

@Composable
private fun PlacementSummary(draft: PlacementDraft) {
    val isCondition = draft.condition == "Кондиция"
    Card(modifier = Modifier.fillMaxWidth(), elevation = 0.dp, backgroundColor = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("К размещению", fontSize = 13.sp)
                Text("${draft.fullQnt} шт.", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isCondition) "Кондиция" else "Некондиция",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCondition) OkColor else MaterialTheme.colors.error
                )
                Text(
                    text = if (draft.expirationDate.isEmpty()) "срок не указан" else "до ${isoToDisplayDate(draft.expirationDate)}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
