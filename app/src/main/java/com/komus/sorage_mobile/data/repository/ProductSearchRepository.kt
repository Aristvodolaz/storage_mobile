package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.StorageDao
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.response.EmptyCellsResponse
import com.komus.sorage_mobile.data.response.LocationProductsResponse
import com.komus.sorage_mobile.data.response.ProductInfo
import com.komus.sorage_mobile.data.response.ProductInfoResponse
import com.komus.sorage_mobile.data.response.ProductSearchResponse
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductSearchRepository @Inject constructor(
    private val api: StorageApi,
    private val dao: StorageDao,
    private val spHelper: SPHelper,
    private val networkUtils: NetworkUtils,
    private val cache: ResponseCache
) {

    suspend fun searchProducts(query: String): Flow<Result<List<ProductInfo>>> = flow {
        try {
            val isShk = query.length > 7
            Log.d("ProductSearchRepository", "Поиск товаров: ${if (isShk) "ШК=$query" else "Артикул=$query"}")

            val shk = if (isShk) query else null
            val article = if (!isShk) query else null
            val apiResponse = cache.fetch(
                key = "product-details:${shk.orEmpty()}:${article.orEmpty()}",
                clazz = ProductSearchResponse::class.java,
                isCacheable = { it.success }
            ) {
                withContext(Dispatchers.IO) { api.getProductDetails(shk = shk, article = article) }
            }
            if (apiResponse.success) {
                emit(Result.success(apiResponse.data.map { it.toProductInfo() }))
            } else {
                emit(Result.failure(Exception("Ошибка поиска товаров")))
            }
        } catch (e: Exception) {
            Log.e("ProductSearchRepository", "Исключение при поиске товаров: ${e.message}")
            emit(Result.failure(e))
        }
    }

    suspend fun getEmptyCells(skladId: Int): List<String> {
        val response = cache.fetch(
            key = "empty-cells:$skladId",
            clazz = EmptyCellsResponse::class.java,
            isCacheable = { it.success }
        ) {
            api.getEmptyCells(skladId)
        }
        return if (response.success) response.data.cells.map { it.name } else emptyList()
    }

    suspend fun searchProductsByLocationId(locationId: String): LocationProductsResponse {
        val skladId = spHelper.getSkladId()
        return cache.fetch(
            key = "location-products:$locationId:$skladId",
            clazz = LocationProductsResponse::class.java,
            isCacheable = { it.success }
        ) {
            api.getLocationProducts(locationId = locationId, skladId = skladId.toInt())
        }
    }

    suspend fun searchProductsByLocationName(locationName: String): LocationProductsResponse {
        val skladId = spHelper.getSkladId()
        return cache.fetch(
            key = "location-products:$locationName:$skladId",
            clazz = LocationProductsResponse::class.java,
            isCacheable = { it.success }
        ) {
            api.getLocationProducts(locationId = locationName, skladId = skladId.toInt())
        }
    }

    suspend fun searchProductsByArticle(article: String): ProductInfoResponse {
        val skladId = spHelper.getSkladId()
        return cache.fetch(
            key = "product-info:article:$article:$skladId",
            clazz = ProductInfoResponse::class.java,
            isCacheable = { it.success }
        ) {
            api.getProductInfo(article = article, skladId = skladId.toInt())
        }
    }

    suspend fun searchProductsByBarcode(barcode: String): ProductInfoResponse {
        val skladId = spHelper.getSkladId()
        return cache.fetch(
            key = "product-info:shk:$barcode:$skladId",
            clazz = ProductInfoResponse::class.java,
            isCacheable = { it.success }
        ) {
            api.getProductInfo(shk = barcode, skladId = skladId.toInt())
        }
    }
}
