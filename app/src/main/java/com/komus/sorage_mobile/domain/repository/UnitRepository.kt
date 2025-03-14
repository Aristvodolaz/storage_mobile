package com.komus.sorage_mobile.domain.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.response.UnitItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UnitRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun fetchUnits(productId: String): Flow<Result<List<UnitItem>>> = flow {
        try {
            val response = api.getUnits(productId)
            if (response.success) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception("Ошибка получения единиц хранения")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}