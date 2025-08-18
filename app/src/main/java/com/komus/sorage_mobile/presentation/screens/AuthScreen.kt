package com.komus.sorage_mobile.presentation.screens



import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.domain.viewModel.AuthViewModel

import androidx.compose.runtime.livedata.observeAsState

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.sorage_mobile.R
import com.komus.sorage_mobile.util.Screen

@Composable
fun AuthScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val isLoading by authViewModel.loading.observeAsState(false)
    val authState by authViewModel.authStatus.collectAsStateWithLifecycle()
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val error by scannerViewModel.error.collectAsStateWithLifecycle()

    var manualInput by remember { mutableStateOf("") }
    val isCipherLabDevice = remember { Build.MANUFACTURER.contains("cipherlab", ignoreCase = true) }

    Log.d("AuthScreen", "Current barcodeData: $barcodeData")

    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("AuthScreen", "Authenticating barcode from DataWedge: $barcodeData")
            authViewModel.authenticate(barcodeData)
            scannerViewModel.clearBarcode()
        }
    }

    LaunchedEffect(manualInput) {
        if (isCipherLabDevice && manualInput.length in 4..64) {
            authViewModel.authenticate(manualInput)
            manualInput = ""
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                scannerViewModel.clearBarcode()
                navController.navigate(Screen.Placement.route) {
                    popUpTo("auth") { inclusive = true }
                }
            }
            is AuthViewModel.AuthState.Error -> {
                val errorMessage = (authState as AuthViewModel.AuthState.Error).error
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    AuthScreenContent(
        isLoading = isLoading,
        manualInput = manualInput,
        onInputChange = { manualInput = it },
        showInputField = isCipherLabDevice
    )
}
@Composable
fun AuthScreenContent(
    isLoading: Boolean,
    manualInput: String,
    onInputChange: (String) -> Unit,
    showInputField: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Добро пожаловать!",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Сканируйте ваш штрих-код для входа.",
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (showInputField) {
            androidx.compose.material3.OutlinedTextField(
                value = manualInput,
                onValueChange = onInputChange,
                label = { Text("Введите или отсканируйте код") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .focusRequester(focusRequester)
            )
            
            // Автофокус при появлении поля ввода
            LaunchedEffect(showInputField) {
                if (showInputField) {
                    kotlinx.coroutines.delay(100) // Небольшая задержка для инициализации UI
                    focusRequester.requestFocus()
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Авторизация...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Text(
                text = if (showInputField) "Ожидание сканирования..." else "Сканер активен",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF008000),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
