package com.komus.sorage_mobile.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.domain.model.InventoryItem
import com.komus.sorage_mobile.domain.model.SearchType
import com.komus.sorage_mobile.domain.state.InventoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    /**
     * Изменение типа поиска
     */
    fun setSearchType(type: SearchType) {
        _uiState.update { it.copy(searchType = type) }
    }

    /**
     * Обновление поискового запроса
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Выполнение поиска
     */
    fun search() {
        val query = _uiState.value.searchQuery.trim()

        if (query.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Введите данные для поиска") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val items = when (_uiState.value.searchType) {
                    SearchType.LOCATION_ID -> repository.getItemsByLocationId(query)
                    SearchType.PRODUCT_ARTICLE -> repository.getIgetItemsByArticletemsBySku(query)
                }

                _uiState.update {
                    it.copy(
                        items = items,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Произошла ошибка при поиске",
                        isLoading = false,
                        items = emptyList()
                    )
                }
            }
        }
    }

    /**
     * Выбор товара из списка
     */
    fun selectItem(item: InventoryItem) {
        _uiState.update {
            it.copy(
                selectedItem = item,
                showDetailsDialog = true
            )
        }
    }

    /**
     * Открытие диалога обновления количества
     */
    fun showUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = true) }
    }

    /**
     * Скрытие всех диалогов
     */
    fun hideDialogs() {
        _uiState.update {
            it.copy(
                showDetailsDialog = false,
                showUpdateDialog = false
            )
        }
    }

    /**
     * Подтверждение товара без изменений
     */
    fun confirmItem() {
        val selectedItem = _uiState.value.selectedItem ?: return

        viewModelScope.launch {
            try {
                val updatedItem = repository.confirmItem(selectedItem)
                updateItemInList(updatedItem)
                hideDialogs()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Ошибка при подтверждении товара",
                        showDetailsDialog = false,
                        showUpdateDialog = false
                    )
                }
            }
        }
    }

    /**
     * Обновление количества товара
     */
    fun updateItemQuantity(newQuantity: Int) {
        val selectedItem = _uiState.value.selectedItem ?: return

        if (newQuantity < 0) {
            _uiState.update { it.copy(errorMessage = "Количество не может быть отрицательным") }
            return
        }

        viewModelScope.launch {
            try {
                val updatedItem = repository.updateItemQuantity(selectedItem, newQuantity)
                updateItemInList(updatedItem)
                hideDialogs()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Ошибка при обновлении количества товара",
                        showDetailsDialog = false,
                        showUpdateDialog = false
                    )
                }
            }
        }
    }

    /**
     * Обновление данных товара
     */
    fun updateItem(
        newQuantity: Int,
        newExpirationDate: String,
        newCondition: String,
        newReason: String?
    ) {
        val selectedItem = _uiState.value.selectedItem ?: return

        if (newQuantity < 0) {
            _uiState.update { it.copy(errorMessage = "Количество не может быть отрицательным") }
            return
        }

        if (newCondition == "Некондиция" && newReason.isNullOrEmpty()) {
            _uiState.update { it.copy(errorMessage = "Необходимо указать причину некондиции") }
            return
        }

        viewModelScope.launch {
            try {
                Log.d("InventoryViewModel", "Обновление товара: количество=$newQuantity, срок годности=$newExpirationDate, состояние=$newCondition, причина=$newReason")
                
                val updatedItem = repository.updateItem(
                    selectedItem,
                    newQuantity,
                    newExpirationDate,
                    newCondition,
                    newReason
                )
                
                updateItemInList(updatedItem)
                _uiState.update { it.copy(updateSuccess = true) }
                hideDialogs()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Ошибка при обновлении товара",
                        showDetailsDialog = false,
                        showUpdateDialog = false
                    )
                }
            }
        }
    }

    /**
     * Обновление товара в списке
     */
    private fun updateItemInList(updatedItem: InventoryItem) {
        val currentItems = _uiState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == updatedItem.id && it.locationId == updatedItem.locationId }

        if (index != -1) {
            currentItems[index] = updatedItem
            _uiState.update { it.copy(items = currentItems) }
        }
    }

    /**
     * Сброс состояния ошибки
     */
    fun resetError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}