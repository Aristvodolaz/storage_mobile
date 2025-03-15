package com.komus.sorage_mobile.domain.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.domain.state.UnitState
import com.komus.sorage_mobile.domain.usecase.FetchUnitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitViewModel @Inject constructor(
    private val fetchUnitsUseCase: FetchUnitsUseCase
) : ViewModel() {

    private val _unitState = MutableStateFlow<UnitState>(UnitState.Idle)
    val unitState: StateFlow<UnitState> = _unitState

    fun fetchUnits(productId: String) {
        viewModelScope.launch {
            _unitState.value = UnitState.Loading
            fetchUnitsUseCase.execute(productId).collect { result ->
                _unitState.value = if (result.isSuccess) {
                    UnitState.Success(result.getOrDefault(emptyList()))
                } else {
                    UnitState.Error(result.exceptionOrNull()?.message ?: "Неизвестная ошибка")
                }
            }
        }
    }
}