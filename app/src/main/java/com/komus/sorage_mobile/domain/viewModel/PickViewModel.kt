package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.domain.state.LocationItemsState
import com.komus.sorage_mobile.domain.state.PickState
import com.komus.sorage_mobile.domain.usecase.GetLocationItemsUseCase
import com.komus.sorage_mobile.domain.usecase.PickFromLocationBySkladIdUseCase
import com.komus.sorage_mobile.domain.usecase.PickFromLocationUseCase
import com.komus.sorage_mobile.domain.usecase.PickProductUseCase
import com.komus.sorage_mobile.domain.usecase.SearchUseCase
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
    private val pickProductUseCase: PickProductUseCase,
    private val pickFromLocationUseCase: PickFromLocationUseCase,
    private val pickFromLocationBySkladIdUseCase: PickFromLocationBySkladIdUseCase,
    private val searchUseCase: SearchUseCase,
    private val spHelper: SPHelper
) : ViewModel() {

    private val _locationItemsState = MutableStateFlow<LocationItemsState>(LocationItemsState.Initial)
    val locationItemsState: StateFlow<LocationItemsState> = _locationItemsState.asStateFlow()
    
    private val _pickState = MutableStateFlow<PickState>(PickState.Initial)
    val pickState: StateFlow<PickState> = _pickState.asStateFlow()
    
    private var _selectedItem: LocationItem? = null
    val selectedItem: LocationItem? get() = _selectedItem
    
    // Добавляем переменную для хранения ID склада
    private var _skladId: String = ""
    
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
    
    fun searchProductByBarcode(barcode: String) {
        Log.d("PickViewModel", "Поиск товара по штрихкоду: $barcode")
        _locationItemsState.value = LocationItemsState.Loading
        
        viewModelScope.launch {
            searchUseCase.execute(shk = barcode, article = null).collectLatest { result ->
                result.fold(
                    onSuccess = { searchItems ->
                        Log.d("PickViewModel", "Найдено ${searchItems.size} товаров по штрихкоду")
                        if (searchItems.isEmpty()) {
                            _locationItemsState.value = LocationItemsState.Error("Товар не найден")
                        } else {
                            // Преобразуем SearchItem в LocationItem для совместимости
                            val locationItems = searchItems.map { searchItem ->
                                LocationItem(
                                    id = searchItem.ID,
                                    name = searchItem.NAME,
                                    article = searchItem.ARTICLE_ID_REAL,
                                    shk = searchItem.SHK,
                                    // Для обратной совместимости
                                    productId = searchItem.ID,
                                    barcode = searchItem.SHK
                                )
                            }
                            _locationItemsState.value = LocationItemsState.Success(locationItems)
                        }
                    },
                    onFailure = { error ->
                        Log.e("PickViewModel", "Ошибка поиска товара: ${error.message}")
                        _locationItemsState.value = LocationItemsState.Error(
                            error.message ?: "Ошибка поиска товара"
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
    
    fun pickProduct(quantity: Int, executor: String) {
        val item = _selectedItem ?: return
        
        Log.d("PickViewModel", "Снятие товара: ${item.productId}, количество: $quantity")
        _pickState.value = PickState.Loading
        
        viewModelScope.launch {
            try {
                val response = item.productId?.let {
                    item.wrShk?.let { it1 ->
                        item.prunitId?.let { it2 ->
                            pickProductUseCase(
                                productId = it,
                                locationId = it1, // Предполагаем, что locationId - это первая часть prunitId
                                prunitId = it2,
                                quantity = quantity,
                                executor = executor
                            )
                        }
                    }
                }

                if (response != null) {
                    if (response.success) {
                        Log.d("PickViewModel", "Товар успешно снят")
                        _pickState.value = PickState.Success
                    } else {
                        Log.e("PickViewModel", "Ошибка снятия товара: ${response.message}")
                        if (response != null) {
                            _pickState.value = PickState.Error(response.message ?: "Ошибка снятия товара")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PickViewModel", "Исключение при снятии товара: ${e.message}")
                _pickState.value = PickState.Error(e.message ?: "Произошла ошибка при снятии товара")
            }
        }
    }
    
    fun pickFromLocation(quantity: Int, executor: String) {
        val item = _selectedItem ?: return
        val locationId = item.wrShk // Предполагаем, что locationId - это первая часть prunitId
        
        Log.d("PickViewModel", "Снятие товара из ячейки: ${item.productId}, ячейка: $locationId, количество: $quantity")
        _pickState.value = PickState.Loading
        
        viewModelScope.launch {
            try {
                val skladId = if (_skladId.isNotEmpty()) _skladId else "85" // Используем значение по умолчанию, если не задано
                
                val response = item.productId?.let {
                    item.prunitId?.let { it1 ->
                        if (locationId != null) {
                            pickFromLocationUseCase(
                                productId = it,
                                wrShk = locationId,
                                prunitId = it1,
                                quantity = quantity,
                                executor = executor,
                                skladId = skladId
                            )
                        } else null
                    }
                }

                if (response != null) {
                    if (response.success) {
                        Log.d("PickViewModel", "Товар успешно снят из ячейки")
                        _pickState.value = PickState.Success
                    } else {
                        Log.e("PickViewModel", "Ошибка снятия товара из ячейки: ${response.message}")
                        _pickState.value = PickState.Error(response.message ?: "Ошибка снятия товара из ячейки")
                    }
                }
            } catch (e: Exception) {
                Log.e("PickViewModel", "Исключение при снятии товара из ячейки: ${e.message}")
                _pickState.value = PickState.Error(e.message ?: "Произошла ошибка при снятии товара из ячейки")
            }
        }
    }
    
    fun pickFromLocationBySkladId(quantity: Int, executor: String) {
        val item = _selectedItem ?: return

        
        Log.d("PickViewModel", "Снятие товара из ячейки с учетом склада: ${item.id}, ячейка: ${item.locationId}, количество: $quantity")
        _pickState.value = PickState.Loading
        
        viewModelScope.launch {
            try {
                val skladId = if (_skladId.isNotEmpty()) _skladId else "85" // Используем значение по умолчанию, если не задано
                
                // Получаем ID ячейки из объекта товара
                val locationIdStr = item.locationId?.toString() ?: "0"
                
                val response = item.wrShk?.let {
                    pickFromLocationBySkladIdUseCase(
                        productId = item.article,
                        wrShk = it,
                        prunitId = item.units.firstOrNull()?.prunitId?.toString() ?: "10",
                        quantity = quantity,
                        executor = executor,
                        skladId = skladId
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
} 