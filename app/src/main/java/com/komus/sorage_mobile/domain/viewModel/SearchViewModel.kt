package com.komus.sorage_mobile.domain.viewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.domain.state.SearchState
import com.komus.sorage_mobile.domain.usecase.SearchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    fun search(shk: String?, article: String?) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            searchUseCase.execute(shk, article).collect { result ->
                _searchState.value = if (result.isSuccess) {
                    SearchState.Success(result.getOrDefault(emptyList()))
                } else {
                    SearchState.Error(result.exceptionOrNull()?.message ?: "Неизвестная ошибка")
                }
            }
        }
    }
}
