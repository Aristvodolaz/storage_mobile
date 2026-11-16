package com.komus.sorage_mobile.domain.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.response.LocationDetailsResponse
import com.komus.sorage_mobile.domain.state.PlacementState
import com.komus.sorage_mobile.domain.usecase.PlaceProductToBufferUseCase
import com.komus.sorage_mobile.data.placement.PlacementDraftStore
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
    private val spHelper: SPHelper,
    private val storageApi: StorageApi,
    private val cache: ResponseCache,
    private val draftStore: PlacementDraftStore
) : ViewModel() {

    private val _state = MutableStateFlow<PlacementState>(PlacementState.Idle)
    val state: StateFlow<PlacementState> = _state.asStateFlow()

    /** Результат проверки отсканированной ячейки. */
    sealed interface LocationCheck {
        object Idle : LocationCheck
        object Checking : LocationCheck
        data class Found(val name: String) : LocationCheck
        object NotFound : LocationCheck
        object Unverified : LocationCheck
        /** Сервер ответил ошибкой: ячейку не проверить, связь при этом есть. */
        object CheckFailed : LocationCheck
    }

    private val _locationCheck = MutableStateFlow<LocationCheck>(LocationCheck.Idle)
    val locationCheck: StateFlow<LocationCheck> = _locationCheck.asStateFlow()

    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    fun fetchLocationName(locationId: String) {
        _locationCheck.value = LocationCheck.Checking
        viewModelScope.launch {
            try {
                val skladId = spHelper.getSkladId()
                val response = cache.fetch(
                    key = "location-details:$locationId:$skladId",
                    clazz = LocationDetailsResponse::class.java,
                    isCacheable = { it.success }
                ) {
                    storageApi.getLocationDetails(locationId, skladId)
                }
                val name = response.data?.locationName
                if (response.success && !name.isNullOrBlank()) {
                    _locationName.value = name
                    _locationCheck.value = LocationCheck.Found(name)
                } else {
                    // Сервер ответил, но такой ячейки нет: размещать нельзя
                    _locationName.value = locationId
                    _locationCheck.value = LocationCheck.NotFound
                }
            } catch (e: java.io.IOException) {
                // Нет связи и нет сохранённых данных: размещение уйдёт в очередь
                Log.e("PlacementViewModel", "Нет связи, ячейку не проверить: ${e.message}")
                _locationName.value = locationId
                _locationCheck.value = LocationCheck.Unverified
            } catch (e: Exception) {
                // Сервер ответил ошибкой: это не отсутствие сети, подтверждать нельзя
                Log.e("PlacementViewModel", "Сервер не смог проверить ячейку: ${e.message}")
                _locationName.value = locationId
                _locationCheck.value = LocationCheck.CheckFailed
            }
        }
    }

    fun clearLocationName() {
        _locationName.value = null
        _locationCheck.value = LocationCheck.Idle
    }

    fun placeProductToBuffer(
        productId: String,
        prunitId: String,
        quantity: Int
    ) {
        // Защита от размещения в непроверенную ячейку, даже если кнопка на экране осталась активной
        val check = _locationCheck.value
        if (check !is LocationCheck.Found && check !is LocationCheck.Unverified) {
            _state.value = PlacementState.Error("Ячейка не подтверждена, отсканируйте её ещё раз")
            return
        }
        _state.value = PlacementState.Loading

        // Все данные размещения берём из одного черновика; склад — текущий склад приложения
        val draft = draftStore.draft.value
        val condition = draft.condition
        val conditionState = if (condition == "Кондиция") "GOOD" else "BAD"
        val reason = if (condition != "Кондиция") draft.reason else ""
        val expirationDate = draft.expirationDate
        val executor = spHelper.getUserName()
        val wrShk = draft.wrShk
        val name = draft.productName
        val shk = draft.shk
        val article = draft.article
        val skladId = spHelper.getSkladId()
        val productQnt = draft.productQnt
        val exPalletType = draft.exPalletType.ifEmpty { null }

        // Автоматически используем состояние "Кондиция" без проверок срока годности

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
        ExPalletType: $exPalletType
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
                productQnt = productQnt,
                exPalletType = exPalletType
            )

            result.fold(
                onSuccess = { response ->
                    Log.d("PlacementViewModel", "Размещение успешно: ${response.message}")
                    val warningMessages = response.warnings
                        ?.mapNotNull { it["message"] }
                        ?: emptyList()
                    _state.value = PlacementState.Success(
                        message = response.message ?: "Товар успешно размещен в буфер",
                        warnings = warningMessages
                    )
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