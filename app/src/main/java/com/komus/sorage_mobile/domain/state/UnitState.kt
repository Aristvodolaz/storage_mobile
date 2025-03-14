package com.komus.sorage_mobile.domain.state

import com.komus.sorage_mobile.data.response.UnitItem

sealed class UnitState {
    object Idle : UnitState()
    object Loading : UnitState()
    data class Success(val data: List<UnitItem>) : UnitState()
    data class Error(val message: String) : UnitState()
}
