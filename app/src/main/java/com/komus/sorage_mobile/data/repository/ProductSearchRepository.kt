package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.response.LocationProductsResponse
import com.komus.sorage_mobile.data.response.ProductInfoResponse
import com.komus.sorage_mobile.data.response.ProductItem
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductSearchRepository @Inject constructor(
    private val api: StorageApi,
    private val spHelper: SPHelper
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

    suspend fun searchProductsByLocationId(locationId: String): LocationProductsResponse {
        val skladId = spHelper.getSkladId()
        return api.getLocationProducts(locationId = locationId, skladId = skladId.toInt())
    }
    
    suspend fun searchProductsByLocationName(locationName: String): LocationProductsResponse {
        // В текущем API нет метода для поиска по имени ячейки, поэтому используем 
        // searchProductsByLocationId, предполагая, что имя может совпадать с ID
        val skladId = spHelper.getSkladId()
        return api.getLocationProducts(locationId = locationName, skladId = skladId.toInt())
    }
    
    suspend fun searchProductsByArticle(article: String): ProductInfoResponse {
        val skladId = spHelper.getSkladId()
        return api.getProductInfo(article = article, skladId = skladId.toInt())
    }
    
    suspend fun searchProductsByBarcode(barcode: String): ProductInfoResponse {
        val skladId = spHelper.getSkladId()
        return api.getProductInfo(shk = barcode,skladId = skladId.toInt())
    }
} 