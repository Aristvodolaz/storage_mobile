package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.state.LocationItemsState
import com.komus.sorage_mobile.domain.state.MoveProductState
import com.komus.sorage_mobile.domain.state.PickState
import com.komus.sorage_mobile.domain.usecase.GetLocationItemsUseCase
import com.komus.sorage_mobile.domain.usecase.MoveProductUseCase
import com.komus.sorage_mobile.domain.usecase.PickFromLocationBySkladIdUseCase
import com.komus.sorage_mobile.domain.usecase.PickFromLocationUseCase
import com.komus.sorage_mobile.domain.usecase.PickProductUseCase
import com.komus.sorage_mobile.domain.usecase.SearchUseCase
import com.komus.sorage_mobile.util.DateUtils
import com.komus.sorage_mobile.util.ProductMovementHelper
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickViewModel @Inject constructor(
    private val getLocationItemsUseCase: GetLocationItemsUseCase,
    private val pickFromLocationBySkladIdUseCase: PickFromLocationBySkladIdUseCase,
    private val moveProductUseCase: MoveProductUseCase,
    private val searchUseCase: SearchUseCase,
    private val spHelper: SPHelper
) : ViewModel() {

    private val _locationItemsState = MutableStateFlow<LocationItemsState>(LocationItemsState.Initial)
    val locationItemsState: StateFlow<LocationItemsState> = _locationItemsState.asStateFlow()
    
    private val _pickState = MutableStateFlow<PickState>(PickState.Initial)
    val pickState: StateFlow<PickState> = _pickState.asStateFlow()
    
    private val _moveProductState = MutableStateFlow<MoveProductState>(MoveProductState.Initial)
    val moveProductState: StateFlow<MoveProductState> = _moveProductState.asStateFlow()
    
    private var _selectedItem: LocationItem? = null
    val selectedItem: LocationItem? get() = _selectedItem
    
    // Добавляем переменную для хранения ID склада
    private var _skladId: String = ""
    
    // Добавляем переменные для хранения исходной и целевой ячеек
    private var _sourceLocationId: String = ""
    private var _targetLocationId: String = ""
    
    fun getLocationItems(locationId: String) {
        Log.d("PickViewModel", "Запрос товаров в ячейке: $locationId")
        _locationItemsState.value = LocationItemsState.Loading
        
        viewModelScope.launch {
            getLocationItemsUseCase(locationId, spHelper.getSkladId()).collectLatest { result ->
                result.fold(
                    onSuccess = { items ->
                        Log.d("PickViewModel", "Получено ${items.size} товаров")
                        if (items.isEmpty()) {
                            _locationItemsState.value = LocationItemsState.Error("Ячейка пуста")
                        } else {
                            _locationItemsState.value = LocationItemsState.Success(items)
                        }
                    },
                    onFailure = { error ->
                        Log.e("PickViewModel", "Ошибка получения товаров: ${error.message}")
                        _locationItemsState.value = LocationItemsState.Error(
                            error.message ?: "Ошибка получения товаров"
                        )
                    }
                )
            }
        }
    }

    fun selectItem(item: LocationItem) {
        _selectedItem = item
    }
    
    fun setSkladId(skladId: String) {
        _skladId = skladId
    }
    

    
    fun pickFromLocationBySkladId(quantity: Int, executor: String) {
        val item = _selectedItem ?: return

        
        Log.d("PickViewModel", "Снятие товара из ячейки с учетом склада: ${item.id}, количество: $quantity")
        _pickState.value = PickState.Loading
        
        viewModelScope.launch {
            try {
                val skladId = if (_skladId.isNotEmpty()) _skladId else "1383" // Используем значение по умолчанию, если не задано

                
                val response = item.wrShk?.let {
                    pickFromLocationBySkladIdUseCase(
                        productId = item.article,
                        wrShk = it,
                        prunitId = item.units.firstOrNull()?.prunitId?.toString() ?: "10",
                        quantity = quantity,
                        executor = executor,
                        skladId = skladId,
                        productQnt = item.units[0].productQnt.toInt()
                    )
                }

                if (response != null) {
                    if (response.success) {
                        Log.d("PickViewModel", "Товар успешно снят из ячейки с учетом склада")
                        _pickState.value = PickState.Success
                    } else {
                        Log.e("PickViewModel", "Ошибка снятия товара из ячейки с учетом склада: ${response.message}")
                        _pickState.value = PickState.Error(response.message ?: "Ошибка снятия товара из ячейки с учетом склада")
                    }
                }
            } catch (e: Exception) {
                Log.e("PickViewModel", "Исключение при снятии товара из ячейки с учетом склада: ${e.message}")
                _pickState.value = PickState.Error(e.message ?: "Произошла ошибка при снятии товара из ячейки с учетом склада")
            }
        }
    }
    
    fun resetPickState() {
        _pickState.value = PickState.Initial
    }
    
    fun resetLocationItemsState() {
        _locationItemsState.value = LocationItemsState.Initial
    }
    
    fun moveProduct(
        quantity: Int,
        conditionState: String,
        executor: String,
        expirationDate: String = "2025-01-14" // Значение по умолчанию
    ) {
        val item = _selectedItem ?: return
        
        if (_sourceLocationId.isEmpty() || _targetLocationId.isEmpty()) {
            _moveProductState.value = MoveProductState.Error("Не указаны исходная или целевая ячейки")
            return
        }
        
        Log.d("PickViewModel", "Перемещение товара: ${item.id}, из ячейки: $_sourceLocationId в ячейку: $_targetLocationId, количество: $quantity")
        _moveProductState.value = MoveProductState.Loading
        
        viewModelScope.launch {
            try {
                val skladId = if (_skladId.isNotEmpty()) _skladId else spHelper.getSkladId()
                val prunitId = item.units.firstOrNull()?.prunitId?.toString() ?: "10"
                
                val response = moveProductUseCase(
                    productId = item.id.toString(),
                    sourceLocationId = _sourceLocationId,
                    targetLocationId = _targetLocationId,
                    prunitId = prunitId,
                    quantity = quantity,
                    conditionState = conditionState,
                    executor = executor,
                    skladId = skladId,
                    expirationDate = expirationDate,
                    productQnt = spHelper.getProductQnt().toString(),
                    reason = ""
                )
                
                if (response.success) {
                    Log.d("PickViewModel", "Товар успешно перемещен")
                    _moveProductState.value = MoveProductState.Success
                } else {
                    Log.e("PickViewModel", "Ошибка при перемещении: ${response}")
                    _moveProductState.value = MoveProductState.Error(
                        (response ?: "Неизвестная ошибка при перемещении товара").toString()
                    )
                }
            } catch (e: Exception) {
                Log.e("PickViewModel", "Исключение при перемещении: ${e.message}", e)
                _moveProductState.value = MoveProductState.Error(
                    e.message ?: "Произошла ошибка при перемещении товара"
                )
            }
        }
    }
    
    fun setSourceLocation(locationId: String) {
        _sourceLocationId = locationId
        Log.d("PickViewModel", "Установлена исходная ячейка: $locationId")
    }
    
    fun setTargetLocation(locationId: String) {
        _targetLocationId = locationId
        Log.d("PickViewModel", "Установлена целевая ячейка: $locationId")
    }
    
    fun resetMoveProductState() {
        _moveProductState.value = MoveProductState.Initial
    }
} 