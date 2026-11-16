package com.komus.sorage_mobile.domain.repository
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.response.SearchItem
import com.komus.sorage_mobile.data.response.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val api: StorageApi,
    private val cache: ResponseCache
) {
    suspend fun searchItem(shk: String?, article: String?): Flow<Result<List<SearchItem>>> = flow {
        try {
            val response = cache.fetch(
                key = "search:${shk.orEmpty()}:${article.orEmpty()}",
                clazz = SearchResponse::class.java,
                isCacheable = { it.success && it.data != null }
            ) {
                withContext(Dispatchers.IO) { api.searchItem(shk, article) }
            }
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                val errorMsg = response.msg ?: "Товар не найден"
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
