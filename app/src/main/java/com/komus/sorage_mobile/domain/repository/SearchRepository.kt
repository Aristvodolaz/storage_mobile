package com.komus.sorage_mobile.domain.repository
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.response.SearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun searchItem(shk: String?, article: String?): Flow<Result<List<SearchItem>>> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                api.searchItem(shk, article)
            }
            if (response.success) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception("Ошибка поиска")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
