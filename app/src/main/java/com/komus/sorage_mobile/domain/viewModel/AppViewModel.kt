package com.komus.sorage_mobile.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val spHelper: SPHelper
) : ViewModel() {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentWarehouse = MutableStateFlow(spHelper.getSkladId())
    val currentWarehouse: StateFlow<String> = _currentWarehouse.asStateFlow()

    init {
        observeNetworkState()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkUtils.observeNetworkState().collect { isConnected ->
                _isConnected.value = isConnected
            }
        }
    }

    fun setWarehouse(warehouseId: String) {
        _currentWarehouse.value = warehouseId
        spHelper.saveSkladId(warehouseId)
    }
} 