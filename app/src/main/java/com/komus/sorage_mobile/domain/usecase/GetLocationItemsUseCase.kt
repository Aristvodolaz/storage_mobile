package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.response.LocationItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationItemsUseCase @Inject constructor(
    private val pickRepository: PickRepository
) {
    suspend operator fun invoke(locationId: String): Flow<Result<List<LocationItem>>> {
        return pickRepository.getLocationItems(locationId)
    }
} 