package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.state.LocationItemsState
import com.komus.sorage_mobile.domain.state.PickState
import com.komus.sorage_mobile.domain.usecase.GetLocationItemsUseCase
import com.komus.sorage_mobile.domain.usecase.PickProductUseCase
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
    private val pickProductUseCase: PickProductUseCase
) : ViewModel() {

    private val _locationItemsState = MutableStateFlow<LocationItemsState>(LocationItemsState.Initial)
    val locationItemsState: StateFlow<LocationItemsState> = _locationItemsState.asStateFlow()
    
    private val _pickState = MutableStateFlow<PickState>(PickState.Initial)
    val pickState: StateFlow<PickState> = _pickState.asStateFlow()
    
    private var _selectedItem: LocationItem? = null
    val selectedItem: LocationItem? get() = _selectedItem
    
    fun getLocationItems(locationId: String) {
        Log.d("PickViewModel", "Запрос товаров в ячейке: $locationId")
        _locationItemsState.value = LocationItemsState.Loading
        
        viewModelScope.launch {
            getLocationItemsUseCase(locationId).collectLatest { result ->
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
    
    fun pickProduct(quantity: Int, executor: String) {
        val item = _selectedItem ?: return
        
        Log.d("PickViewModel", "Снятие товара: ${item.productId}, количество: $quantity")
        _pickState.value = PickState.Loading
        
        viewModelScope.launch {
            try {
                val response = pickProductUseCase(
                    productId = item.productId,
                    locationId = item.prunitId.split("-")[0], // Предполагаем, что locationId - это первая часть prunitId
                    prunitId = item.prunitId,
                    quantity = quantity,
                    executor = executor
                )
                
                if (response.success) {
                    Log.d("PickViewModel", "Товар успешно снят")
                    _pickState.value = PickState.Success
                } else {
                    Log.e("PickViewModel", "Ошибка снятия товара: ${response.message}")
                    _pickState.value = PickState.Error(response.message ?: "Ошибка снятия товара")
                }
            } catch (e: Exception) {
                Log.e("PickViewModel", "Исключение при снятии товара: ${e.message}")
                _pickState.value = PickState.Error(e.message ?: "Произошла ошибка при снятии товара")
            }
        }
    }
    
    fun resetPickState() {
        _pickState.value = PickState.Initial
    }
    
    fun resetLocationItemsState() {
        _locationItemsState.value = LocationItemsState.Initial
    }
} 