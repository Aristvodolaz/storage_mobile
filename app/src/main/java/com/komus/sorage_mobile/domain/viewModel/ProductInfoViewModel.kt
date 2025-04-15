package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.repository.ProductSearchRepository
import com.komus.sorage_mobile.data.response.LocationProduct
import com.komus.sorage_mobile.data.response.ProductInfo
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProductInfoViewModel"
@HiltViewModel
class ProductInfoViewModel @Inject constructor(
    private val productSearchRepository: ProductSearchRepository,
    private val  spHelper: SPHelper
) : ViewModel() {

    // Типы поиска, которые пользователь может выбрать
    enum class SearchType {
        LOCATION_ID,
        LOCATION_NAME,
        ARTICLE,
        EMPTY_CELLS  // Новый тип поиска
    }


    data class UiState(
        val searchType: SearchType = SearchType.LOCATION_ID,
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val locationProducts: List<LocationProduct> = emptyList(),
        val productInfo: ProductInfo? = null,
        val emptyCells: List<String> = emptyList(),  // Новое поле
        val errorMessage: String? = null,
        val isEmpty: Boolean = false
    )

    // MVI: События UI
    sealed class UiEvent {
        data class OnSearchTypeChanged(val searchType: SearchType) : UiEvent()
        data class OnSearchQueryChanged(val query: String) : UiEvent()
        object OnSearchClicked : UiEvent()
        object OnErrorDismissed : UiEvent()
    }

    // MVI: Эффекты
    sealed class UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEffect = MutableStateFlow<UiEffect?>(null)
    val uiEffect: StateFlow<UiEffect?> = _uiEffect

    // Обработка UI событий
    fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.OnSearchTypeChanged -> {
                _uiState.update { it.copy(searchType = event.searchType, errorMessage = null) }
            }
            is UiEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }

                // Если запрос пустой - сбрасываем результаты
                if (event.query.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            locationProducts = emptyList(),
                            productInfo = null,
                            isEmpty = false,
                            errorMessage = null
                        )
                    }
                    return
                }

                // Минимальная длина запроса для автоматического поиска
                if (event.query.length >= 3) {
                    performSearch()
                }
            }
            UiEvent.OnSearchClicked -> performSearch()
            UiEvent.OnErrorDismissed -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }
    private fun performSearch() {
        val query = _uiState.value.searchQuery

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                when (_uiState.value.searchType) {
                    SearchType.LOCATION_ID -> searchByLocationId(query)
                    SearchType.LOCATION_NAME -> searchByLocationName(query)
                    SearchType.ARTICLE -> searchByArticle(query)
                    SearchType.EMPTY_CELLS -> searchEmptyCells() // добавлено
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при поиске: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка: ${e.message ?: "Неизвестная ошибка"}",
                        isEmpty = false
                    )
                }
                _uiEffect.value = UiEffect.ShowSnackbar("Ошибка при получении данных")
            }
        }
    }

    private suspend fun searchEmptyCells() {
        val skladId = spHelper.getSkladId().toInt()
        val emptyCells = productSearchRepository.getEmptyCells(skladId)

        _uiState.update {
            it.copy(
                isLoading = false,
                emptyCells = emptyCells,
                isEmpty = emptyCells.isEmpty(),
                errorMessage = if (emptyCells.isEmpty()) "Пустых ячеек нет" else null
            )
        }
    }



    private suspend fun searchByLocationId(locationId: String) {
        val response = productSearchRepository.searchProductsByLocationId(locationId)

        if (response.success) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    locationProducts = response.data,
                    productInfo = null,
                    isEmpty = response.data.isEmpty(),
                    errorMessage = null
                )
            }

            if (response.data.isEmpty()) {
                _uiEffect.value = UiEffect.ShowSnackbar("По запросу ничего не найдено")
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = response.message ?: "Ошибка при поиске товаров в ячейке",
                    isEmpty = false
                )
            }
            _uiEffect.value = UiEffect.ShowSnackbar(response.message ?: "Ошибка при поиске товаров")
        }
    }

    private suspend fun searchByLocationName(locationName: String) {
        val response = productSearchRepository.searchProductsByLocationName(locationName)

        if (response.success) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    locationProducts = response.data,
                    productInfo = null,
                    isEmpty = response.data.isEmpty(),
                    errorMessage = null
                )
            }

            if (response.data.isEmpty()) {
                _uiEffect.value = UiEffect.ShowSnackbar("По запросу ничего не найдено")
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = response.message ?: "Ошибка при поиске товаров по названию ячейки",
                    isEmpty = false
                )
            }
            _uiEffect.value = UiEffect.ShowSnackbar(response.message ?: "Ошибка при поиске товаров")
        }
    }

    private suspend fun searchByArticle(article: String) {
        val response = productSearchRepository.searchProductsByArticle(article)

        if (response.success && response.data != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    locationProducts = emptyList(),
                    productInfo = response.data,
                    isEmpty = response.data.items.isEmpty(),
                    errorMessage = null
                )
            }

            if (response.data.items.isEmpty()) {
                _uiEffect.value = UiEffect.ShowSnackbar("По артикулу ничего не найдено")
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = response.message ?: "Ошибка при поиске товара по артикулу",
                    isEmpty = false
                )
            }
            _uiEffect.value = UiEffect.ShowSnackbar(response.message ?: "Ошибка при поиске товара")
        }
    }
}
