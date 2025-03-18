package com.komus.sorage_mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.komus.scanner_module.ScannerViewModel
import com.komus.sorage_mobile.presentation.screens.MainScreen
import com.komus.sorage_mobile.ui.theme.Sorage_mobileTheme
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val scannerViewModel: ScannerViewModel by viewModels()

    @Inject
    lateinit var spHelper: SPHelper
    private val scanReceiver = createScanReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Настройка отображения на весь экран
        enableEdgeToEdge()
        // Настройка отображения контента под системными элементами
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Установка прозрачности статус-бара и навигационной панели
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        registerScanReceiver()

        setContent {
            Sorage_mobileTheme {
                MainScreen(spHelper, scannerViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanReceiver)
    }

    private fun createScanReceiver(): BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val scannedData = intent.getStringExtra("com.symbol.datawedge.data_string")
            if (!scannedData.isNullOrEmpty()) {
                scannerViewModel.onBarcodeScanned(scannedData)
            }
        }
    }

    private fun registerScanReceiver() {
        val intentFilter = IntentFilter("com.symbol.datawedge.api.RESULT_ACTION")
        
        // Начиная с Android 14 (API 34) необходимо указывать флаг экспорта
        if (Build.VERSION.SDK_INT >= 34) { // Android 14 (UPSIDE_DOWN_CAKE)
            registerReceiver(scanReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(scanReceiver, intentFilter)
        }
    }
}