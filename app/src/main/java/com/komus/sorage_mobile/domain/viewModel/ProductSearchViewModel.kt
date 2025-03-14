package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.domain.state.ProductSearchState
import com.komus.sorage_mobile.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductSearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {

    private val _searchState = MutableStateFlow<ProductSearchState>(ProductSearchState.Initial)
    val searchState: StateFlow<ProductSearchState> = _searchState.asStateFlow()

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _searchState.value = ProductSearchState.Error("Введите артикул или штрихкод товара")
            return
        }

        Log.d("ProductSearchViewModel", "Поиск товаров: $query")
        _searchState.value = ProductSearchState.Loading

        viewModelScope.launch {
            searchProductsUseCase(query).collectLatest { result ->
                result.fold(
                    onSuccess = { products ->
                        Log.d("ProductSearchViewModel", "Найдено ${products.size} товаров")
                        if (products.isEmpty()) {
                            _searchState.value = ProductSearchState.Error("Товары не найдены")
                        } else {
                            _searchState.value = ProductSearchState.Success(products)
                        }
                    },
                    onFailure = { error ->
                        Log.e("ProductSearchViewModel", "Ошибка поиска товаров: ${error.message}")
                        _searchState.value = ProductSearchState.Error(
                            error.message ?: "Ошибка поиска товаров"
                        )
                    }
                )
            }
        }
    }

    fun resetSearchState() {
        _searchState.value = ProductSearchState.Initial
    }
} 