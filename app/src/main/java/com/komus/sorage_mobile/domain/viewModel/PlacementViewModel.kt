package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.domain.state.PlacementState
import com.komus.sorage_mobile.domain.usecase.PlaceProductToBufferUseCase
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacementViewModel @Inject constructor(
    private val placeProductToBufferUseCase: PlaceProductToBufferUseCase,
    private val spHelper: SPHelper
) : ViewModel() {

    private val _state = MutableStateFlow<PlacementState>(PlacementState.Idle)
    val state: StateFlow<PlacementState> = _state.asStateFlow()

    fun placeProductToBuffer(
        productId: String,
        prunitId: String,
        quantity: Int
    ) {
        _state.value = PlacementState.Loading
        
        // Получаем ячейку буфера из SPHelper (только для логирования)
        val bufferLocation = spHelper.getBufferLocation()
        Log.d("PlacementViewModel", "Ячейка буфера: $bufferLocation")
        
        // Получаем состояние товара из SPHelper
        val condition = spHelper.getCondition()
        val conditionState = if (condition == "Кондиция") "GOOD" else "BAD"
        Log.d("PlacementViewModel", "Состояние товара: $condition -> $conditionState")
        
        // Получаем причину некондиции из SPHelper, если товар некондиционный
        val reason = if (condition != "Кондиция") spHelper.getReason() else ""
        Log.d("PlacementViewModel", "Причина некондиции: $reason")
        
        // Получаем срок годности из SPHelper
        val expirationDate = spHelper.getSrokGodnosti()
        Log.d("PlacementViewModel", "Срок годности: $expirationDate")
        
        // Получаем имя пользователя из SPHelper
        val executor = spHelper.getUserName()
        Log.d("PlacementViewModel", "Исполнитель: $executor")
        
        // Получаем дополнительные данные из SPHelper
        val wrShk = spHelper.getWrShk()
        val name = spHelper.getProductName()
        val shk = spHelper.getShk()
        val article = spHelper.getArticle()
        val skladId = spHelper.getSkladId()
        val productQnt = spHelper.getProductQnt()
        
        Log.d("PlacementViewModel", "Дополнительные данные: wrShk=$wrShk, name=$name, shk=$shk, article=$article, skladId=$skladId")
        
        viewModelScope.launch {
            val result = placeProductToBufferUseCase.execute(
                productId = productId,
                prunitId = prunitId,
                quantity = quantity,
                conditionState = conditionState,
                expirationDate = expirationDate,
                executor = executor,
                wrShk = wrShk,
                name = name,
                shk = shk,
                article = article,
                skladId = skladId,
                reason = reason,
                productQnt = productQnt
            )
            
            result.fold(
                onSuccess = { response ->
                    _state.value = PlacementState.Success(response.message ?: "Товар успешно размещен в буфер")
                },
                onFailure = { error ->
                    _state.value = PlacementState.Error(error.message ?: "Ошибка размещения товара")
                }
            )
        }
    }
    
    fun resetState() {
        _state.value = PlacementState.Idle
    }
} 