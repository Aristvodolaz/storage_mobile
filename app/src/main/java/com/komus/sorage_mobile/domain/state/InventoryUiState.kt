package com.komus.sorage_mobile.domain.state

import com.komus.sorage_mobile.domain.model.InventoryItem
import com.komus.sorage_mobile.domain.model.SearchType

/**
 * Состояние UI для экрана инвентаризации
 */
data class InventoryUiState(
    val searchType: SearchType = SearchType.LOCATION_ID,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val items: List<InventoryItem> = emptyList(),
    val selectedItem: InventoryItem? = null,
    val showUpdateDialog: Boolean = false,
    val showDetailsDialog: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false,
    val isSyncing: Boolean = false,
    val hasUnsyncedChanges: Boolean = false,
    val lastSyncTime: Long = 0
) 