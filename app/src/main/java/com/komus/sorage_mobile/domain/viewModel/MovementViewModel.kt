package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.repository.MovementRepository
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.data.response.ItemUnit
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.util.DateUtils
import com.komus.sorage_mobile.util.ProductMovementHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MovementViewModel"

@HiltViewModel
class MovementViewModel @Inject constructor(
    private val movementRepository: MovementRepository,
    private val spHelper: SPHelper
) : ViewModel() {

    // Состояния для экранов перемещения
    sealed class LocationItemsState {
        object Loading : LocationItemsState()
        data class Success(val items: List<LocationItem>) : LocationItemsState()
        data class Error(val message: String) : LocationItemsState()
        object Empty : LocationItemsState()
    }

    sealed class MoveProductState {
        object Initial : MoveProductState()
        object Loading : MoveProductState()
        object Success : MoveProductState()
        data class Error(val message: String) : MoveProductState()
    }

    // StateFlow для отслеживания состояний
    private val _locationItemsState = MutableStateFlow<LocationItemsState>(LocationItemsState.Empty)
    val locationItemsState: StateFlow<LocationItemsState> = _locationItemsState

    private val _moveProductState = MutableStateFlow<MoveProductState>(MoveProductState.Initial)
    val moveProductState: StateFlow<MoveProductState> = _moveProductState

    // Данные для перемещения
    private val _selectedItem = MutableLiveData<LocationItem>()
    val selectedItem: LiveData<LocationItem> = _selectedItem

    private val _selectedUnit = MutableLiveData<ItemUnit>()
    val selectedUnit: LiveData<ItemUnit> = _selectedUnit

    private val _sourceLocation = MutableLiveData<String>()
    val sourceLocation: LiveData<String> = _sourceLocation

    private val _targetLocation = MutableLiveData<String>()
    val targetLocation: LiveData<String> = _targetLocation

    private val _moveQuantity = MutableLiveData<Int>()
    val moveQuantity: LiveData<Int> = _moveQuantity

    // Получение списка товаров в исходной ячейке
    fun getLocationItems(locationId: String) {
        Log.d(TAG, "Запрос товаров из ячейки: $locationId")
        _locationItemsState.value = LocationItemsState.Loading
        _sourceLocation.value = locationId
        
        viewModelScope.launch {
            try {
                val response = movementRepository.getLocationItems(locationId)
                Log.d(TAG, "API ответ: успех=${response.success}, найдено ${response.data.size} товаров")
                
                if (response.success && response.data.isNotEmpty()) {
                    _locationItemsState.value = LocationItemsState.Success(response.data)
                } else if (response.success && response.data.isEmpty()) {
                    _locationItemsState.value = LocationItemsState.Empty
                } else {
                    _locationItemsState.value = LocationItemsState.Error(
                        response.message ?: "Не удалось получить список товаров"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении товаров: ${e.message}", e)
                _locationItemsState.value = LocationItemsState.Error(
                    e.message ?: "Произошла ошибка при получении списка товаров"
                )
            }
        }
    }

    // Выбор товара и его единицы измерения
    fun selectItem(item: LocationItem) {
        Log.d(TAG, "Выбран товар: ${item.name} (ID: ${item.id})")
        _selectedItem.value = item
        
        // По умолчанию выбираем первую единицу измерения
        item.units.firstOrNull()?.let { unit ->
            _selectedUnit.value = unit
            Log.d(TAG, "Выбрана единица измерения: ${unit.prunitName}, количество: ${unit.quantity}")
        }
    }

    // Выбор единицы измерения для товара
    fun selectUnit(unit: ItemUnit) {
        Log.d(TAG, "Выбрана единица измерения: ${unit.prunitName}, количество: ${unit.quantity}")
        _selectedUnit.value = unit
    }

    // Установка количества для перемещения
    fun setMoveQuantity(quantity: Int): Boolean {
        val unit = _selectedUnit.value ?: return false
        val availableQuantity = unit.quantity.toIntOrNull() ?: 0
        
        return if (quantity > 0 && quantity <= availableQuantity) {
            _moveQuantity.value = quantity
            Log.d(TAG, "Установлено количество для перемещения: $quantity")
            true
        } else {
            Log.e(TAG, "Неверное количество: $quantity (доступно: $availableQuantity)")
            false
        }
    }

    // Установка целевой ячейки
    fun setTargetLocation(locationId: String): Boolean {
        val sourceLocationId = _sourceLocation.value
        
        return if (locationId != sourceLocationId) {
            _targetLocation.value = locationId
            Log.d(TAG, "Установлена целевая ячейка: $locationId")
            true
        } else {
            Log.e(TAG, "Ошибка: целевая ячейка совпадает с исходной")
            false
        }
    }

    // Перемещение товара
    fun moveProduct() {
        val item = _selectedItem.value ?: return
        val unit = _selectedUnit.value ?: return
        val sourceLocationId = _sourceLocation.value ?: return
        val targetLocationId = _targetLocation.value ?: return
        val quantity = _moveQuantity.value ?: return
        
        Log.d(TAG, "Начало перемещения товара: ${item.name} из $sourceLocationId в $targetLocationId (количество: $quantity)")
        _moveProductState.value = MoveProductState.Loading
        
        viewModelScope.launch {
            try {
                val response = movementRepository.moveProduct(
                    productId = item.article,
                    sourceLocationId = sourceLocationId,
                    targetLocationId = targetLocationId,
                    quantity = quantity,
                    conditionState = unit.conditionState,
                    expirationDate = unit.expirationDate ?: ProductMovementHelper.DEFAULT_EXPIRATION_DATE,
                    executor = spHelper.getUserName() ?: "Пользователь",
                    prunitId = unit.prunitId.toString(),
                    productQnt = spHelper.getProductQnt().toString()
                )
                
                if (response.success) {
                    Log.d(TAG, "Товар успешно перемещен")
                    _moveProductState.value = MoveProductState.Success
                } else {
                    Log.e(TAG, "Ошибка при перемещении: ${response.message}")
                    _moveProductState.value = MoveProductState.Error(
                        response.message ?: "Неизвестная ошибка при перемещении товара"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при перемещении: ${e.message}", e)
                _moveProductState.value = MoveProductState.Error(
                    e.message ?: "Произошла ошибка при перемещении товара"
                )
            }
        }
    }

    // Сброс всех данных перемещения
    fun resetMovementData() {
        Log.d(TAG, "Сброс данных перемещения")
        _selectedItem.value = null
        _selectedUnit.value = null
        _sourceLocation.value = null
        _targetLocation.value = null
        _moveQuantity.value = null
        _locationItemsState.value = LocationItemsState.Empty
        _moveProductState.value = MoveProductState.Initial
    }

    // Сброс результата перемещения для следующей операции
    fun resetMoveResult() {
        _moveProductState.value = MoveProductState.Initial
    }
} 