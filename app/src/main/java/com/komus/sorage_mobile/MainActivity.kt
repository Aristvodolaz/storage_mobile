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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
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
        
        // Настройка отображения с учетом системных элементов
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Sorage_mobileTheme {
        Greeting("Android")
    }
}