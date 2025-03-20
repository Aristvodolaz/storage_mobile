package com.komus.sorage_mobile.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.entity.OfflinePlacementEntity
import com.komus.sorage_mobile.data.db.entity.StorageItemEntity
import com.komus.sorage_mobile.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OfflinePlacementViewModel @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _placementState = MutableStateFlow<PlacementState>(PlacementState.Initial)
    val placementState: StateFlow<PlacementState> = _placementState

    private val _productState = MutableStateFlow<ProductState>(ProductState.Initial)
    val productState: StateFlow<ProductState> = _productState

    fun findProduct(articleOrBarcode: String, isArticleMode: Boolean) {
        viewModelScope.launch {
            try {
                val product = storageRepository.findItemByArticleOrBarcode(
                    article = if (isArticleMode) articleOrBarcode else "",
                    barcode = if (!isArticleMode) articleOrBarcode else ""
                )
                
                if (product != null) {
                    _productState.value = ProductState.Found(product)
                } else {
                    _productState.value = ProductState.NotFound
                }
            } catch (e: Exception) {
                _productState.value = ProductState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun savePlacement(
        articleOrBarcode: String,
        isArticleMode: Boolean,
        prunitTypeId: Int,
        quantity: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        isGoodCondition: Boolean,
        reason: String?,
        cellBarcode: String,
        productQnt: Int
    ) {
        viewModelScope.launch {
            try {
                val placement = OfflinePlacementEntity(
                    id = UUID.randomUUID().toString(),
                    article = if (isArticleMode) articleOrBarcode else "",
                    barcode = if (!isArticleMode) articleOrBarcode else "",
                    prunitTypeId = prunitTypeId,
                    quantity = quantity,
                    endDate = endDate.toString(),
                    condition = isGoodCondition.toString(),
                    reason = reason ?: "",
                    cellBarcode = cellBarcode,
                    timestamp = System.currentTimeMillis(),
                    isSynced = false,
                    productQnt = productQnt,
                    packageType = prunitTypeId.toString()
                )

                inventoryDao.insertOfflinePlacement(placement)
                _placementState.value = PlacementState.Success
            } catch (e: Exception) {
                _placementState.value = PlacementState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

}

sealed class PlacementState {
    object Initial : PlacementState()
    object Success : PlacementState()
    data class Error(val message: String) : PlacementState()
}

sealed class ProductState {
    object Initial : ProductState()
    data class Found(val product: StorageItemEntity) : ProductState()
    object NotFound : ProductState()
    data class Error(val message: String) : ProductState()
} 