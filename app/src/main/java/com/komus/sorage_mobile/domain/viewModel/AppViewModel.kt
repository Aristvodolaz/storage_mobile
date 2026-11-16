package com.komus.sorage_mobile.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.offline.OfflineQueue
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
    private val spHelper: SPHelper,
    private val offlineQueue: OfflineQueue
) : ViewModel() {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentWarehouse = MutableStateFlow(spHelper.getSkladId())
    val currentWarehouse: StateFlow<String> = _currentWarehouse.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _failedOperations = MutableStateFlow<List<PendingOperationEntity>>(emptyList())
    val failedOperations: StateFlow<List<PendingOperationEntity>> = _failedOperations.asStateFlow()

    init {
        observeNetworkState()
        observeOfflineQueue()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkUtils.observeNetworkState().collect { isConnected ->
                _isConnected.value = isConnected
            }
        }
    }

    private fun observeOfflineQueue() {
        viewModelScope.launch {
            offlineQueue.pendingCount().collect { _pendingCount.value = it }
        }
        viewModelScope.launch {
            offlineQueue.failedOperations().collect { _failedOperations.value = it }
        }
    }

    fun retryOperation(id: Long) {
        viewModelScope.launch { offlineQueue.retry(id) }
    }

    fun discardOperation(id: Long) {
        viewModelScope.launch { offlineQueue.discard(id) }
    }

    fun setWarehouse(warehouseId: String) {
        _currentWarehouse.value = warehouseId
        spHelper.saveSkladId(warehouseId)
    }
}
