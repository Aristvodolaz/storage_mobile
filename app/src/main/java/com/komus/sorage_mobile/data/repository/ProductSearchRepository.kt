package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.response.ProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductSearchRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun searchProducts(query: String): Flow<Result<List<ProductItem>>> = flow {
        try {
            val isShk = query.length > 7
            Log.d("ProductSearchRepository", "Поиск товаров: ${if (isShk) "ШК=$query" else "Артикул=$query"}")
            
            val response = withContext(Dispatchers.IO) {
                api.getProductDetails(
                    shk = if (isShk) query else null,
                    article = if (!isShk) query else null
                )
            }
            
            if (response.success) {
                Log.d("ProductSearchRepository", "Найдено ${response.data.size} товаров")
                emit(Result.success(response.data))
            } else {
                Log.e("ProductSearchRepository", "Ошибка поиска товаров")
                emit(Result.failure(Exception("Ошибка поиска товаров")))
            }
        } catch (e: Exception) {
            Log.e("ProductSearchRepository", "Исключение при поиске товаров: ${e.message}")
            emit(Result.failure(e))
        }
    }
} 