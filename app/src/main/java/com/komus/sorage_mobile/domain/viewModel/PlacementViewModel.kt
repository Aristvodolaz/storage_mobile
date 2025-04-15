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

        val bufferLocation = spHelper.getBufferLocation()
        val condition = spHelper.getCondition()
        val conditionState = if (condition == "Кондиция") "GOOD" else "BAD"
        val reason = if (condition != "Кондиция") spHelper.getReason() else ""
        val expirationDate = spHelper.getSrokGodnosti()
        val executor = spHelper.getUserName()
        val wrShk = spHelper.getWrShk()
        val name = spHelper.getProductName()
        val shk = spHelper.getShk()
        val article = spHelper.getArticle()
        val skladId = spHelper.getSkladId()
        val productQnt = spHelper.getProductQnt()

        Log.d("PlacementViewModel", """
        Размещение товара в буфер:
        ProductId: $productId
        PrunitId: $prunitId
        Quantity: $quantity
        ConditionState: $conditionState
        ExpirationDate: $expirationDate
        Executor: $executor
        WrShk: $wrShk
        Name: $name
        SHK: $shk
        Article: $article
        SkladId: $skladId
        Reason: $reason
        ProductQnt: $productQnt
    """.trimIndent())

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
                    Log.d("PlacementViewModel", "Размещение успешно: ${response.message}")
                    _state.value = PlacementState.Success(response.message ?: "Товар успешно размещен в буфер")
                },
                onFailure = { error ->
                    Log.e("PlacementViewModel", "Ошибка размещения товара: ${error.message}")
                    _state.value = PlacementState.Error(error.message ?: "Ошибка размещения товара")
                }
            )
        }
    }


    fun resetState() {
        _state.value = PlacementState.Idle
    }
} 