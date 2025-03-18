package com.komus.scanner_module


import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _barcodeData = MutableStateFlow("")
    val barcodeData: StateFlow<String> get() = _barcodeData

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> get() = _error
    
    // Добавляем поток для хранения отсканированного значения
    private val _scannedValue = MutableStateFlow("")
    val scannedValue: StateFlow<String> get() = _scannedValue

    fun onBarcodeScanned(data: String) {
        Log.d("ScannerViewModel", "Updating barcode data: $data")
        _barcodeData.value = data
    }

    fun clearBarcode() {
        _barcodeData.value = ""
        _error.value = ""
    }

    fun onScanError(errorMessage: String) {
        Log.d("ScannerViewModel", "Updating error: $errorMessage")
        _error.value = errorMessage
    }
    
    // Метод для установки отсканированного значения
    fun setScannedValue(value: String) {
        Log.d("ScannerViewModel", "Setting scanned value: $value")
        _scannedValue.value = value
    }
    
    // Метод для очистки отсканированного значения
    fun clearScannedValue() {
        _scannedValue.value = ""
    }
    
    // Метод для получения имени пользователя
    fun getUserName(): String {
        // Здесь должна быть логика получения имени пользователя из SharedPreferences
        // Пока возвращаем заглушку
        return "Пользователь"
    }
}