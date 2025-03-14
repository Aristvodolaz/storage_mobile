package com.komus.sorage_mobile.presentation.screens



import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AuthScreen(
    navController: NavController,
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // Состояния
    val isLoading by authViewModel.loading.observeAsState(false)
    val authState by authViewModel.authStatus.collectAsStateWithLifecycle()
    val barcodeData by scannerViewModel.barcodeData.collectAsStateWithLifecycle()
    val error by scannerViewModel.error.collectAsStateWithLifecycle()

    Log.d("AuthScreen", "Current barcodeData: $barcodeData")

    LaunchedEffect(barcodeData) {
        if (barcodeData.isNotEmpty()) {
            Log.d("AuthScreen", "Authenticating barcode: $barcodeData")
            authViewModel.authenticate(barcodeData)
            scannerViewModel.clearBarcode()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                val username = (authState as AuthViewModel.AuthState.Success).user.name
                scannerViewModel.clearBarcode() // Сброс после обработки
                Log.d("AuthScreen", "Authentication success. Navigating to home: $username")
                navController.navigate("task") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            is AuthViewModel.AuthState.Error -> {
                val errorMessage = (authState as AuthViewModel.AuthState.Error).error
                Log.e("AuthScreen", "Authentication error: $errorMessage")
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    AuthScreenContent(isLoading = isLoading)
}

@Composable
fun AuthScreenContent(
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Логотип
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 24.dp)
        )

        // Заголовок
        Text(
            text = "Добро пожаловать!",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Описание
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

        Spacer(modifier = Modifier.height(32.dp))

        // Индикатор загрузки или текст
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
                text = "Сканер активен",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF008000),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}