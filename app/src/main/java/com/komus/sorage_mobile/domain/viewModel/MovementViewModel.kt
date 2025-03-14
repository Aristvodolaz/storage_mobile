package com.komus.sorage_mobile.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.repository.MovementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovementViewModel @Inject constructor(
    private val movementRepository: MovementRepository
) : ViewModel() {

    fun moveProduct(
        productId: String,
        sourceLocationId: String,
        targetLocationId: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = movementRepository.moveProduct(
                    productId = productId,
                    sourceLocationId = sourceLocationId,
                    targetLocationId = targetLocationId,
                    quantity = quantity
                )
                
                if (response.success) {
                    onSuccess()
                } else {
                    onError(response.message ?: "Неизвестная ошибка")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Произошла ошибка при перемещении товара")
            }
        }
    }
} 