package com.komus.sorage_mobile.domain.viewModel

import androidx.lifecycle.ViewModel
import com.komus.sorage_mobile.data.placement.PlacementDraft
import com.komus.sorage_mobile.data.placement.PlacementDraftStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Доступ экранов размещения к общему черновику. */
@HiltViewModel
class PlacementFlowViewModel @Inject constructor(
    private val store: PlacementDraftStore
) : ViewModel() {

    val draft: StateFlow<PlacementDraft> = store.draft

    fun update(transform: (PlacementDraft) -> PlacementDraft) = store.update(transform)

    fun clear() = store.clear()
}
