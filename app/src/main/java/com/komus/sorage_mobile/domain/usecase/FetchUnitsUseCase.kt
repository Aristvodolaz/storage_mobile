package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.response.UnitItem
import com.komus.sorage_mobile.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchUnitsUseCase @Inject constructor(
    private val repository: UnitRepository
) {
    suspend fun execute(productId: String): Flow<Result<List<UnitItem>>> {
        return repository.fetchUnits(productId)
    }
}