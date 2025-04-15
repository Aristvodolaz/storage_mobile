package com.komus.sorage_mobile.data.repository

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.dao.StorageDao
import com.komus.sorage_mobile.data.model.StorageItem
import com.komus.sorage_mobile.data.response.LocationProduct
import com.komus.sorage_mobile.data.response.LocationProductsResponse
import com.komus.sorage_mobile.data.response.ProductInfo
import com.komus.sorage_mobile.data.response.ProductInfoResponse
import com.komus.sorage_mobile.data.response.ProductItem
import com.komus.sorage_mobile.data.response.ProductLocation
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
    private val networkUtils: NetworkUtils
) {

    suspend fun searchProducts(query: String): Flow<Result<List<ProductInfo>>> = flow {
        try {
            val isShk = query.length > 7
            Log.d("ProductSearchRepository", "Поиск товаров: ${if (isShk) "ШК=$query" else "Артикул=$query"}")

            if (networkUtils.isNetworkAvailable()) {
                // Если есть интернет, ищем через API
                val apiResponse = withContext(Dispatchers.IO) {
                    api.getProductDetails(
                        shk = if (isShk) query else null,
                        article = if (!isShk) query else null
                    )
                }
                if (apiResponse.success) {
                    Result.success(apiResponse.data) // Emit API response
                } else {
                    emit(Result.failure(Exception("Ошибка поиска товаров")))
                }
            } else {
                // Если нет интернета, ищем в базе данных
                Log.d("ProductSearchRepository", "Нет интернета, ищем в локальной базе данных")
                val localData = searchProductsFromDatabase(query)
                emit(Result.success(localData)) // Emit local data if no internet
            }

        } catch (e: Exception) {
            Log.e("ProductSearchRepository", "Исключение при поиске товаров: ${e.message}")
            emit(Result.failure(e))
        }
    }

    suspend fun getEmptyCells(skladId: Int): List<String> {
        return if (networkUtils.isNetworkAvailable()) {
            val response = api.getEmptyCells(skladId)
            if (response.success) {
                response.data.cells.map { it.name }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private suspend fun searchProductsFromDatabase(query: String): List<ProductInfo> {
        val storageItems = if (query.length > 7) {
            dao.findItemsByShk(query)
        } else {
            dao.findItemsByArticle(query)
        }

        return storageItems.map { storageItemEntity ->
            ProductInfo(
                id = storageItemEntity.id,
                name = storageItemEntity.name,
                shk = storageItemEntity.shk,
                article = storageItemEntity.article,
                totalQuantity = storageItemEntity.productQnt,
                items = listOf(
                    ProductLocation(
                        id = storageItemEntity.id,
                        name_wr = storageItemEntity.wrShk,
                        quantity = storageItemEntity.placeQnt,
                        conditionState = storageItemEntity.conditionState,
                        expirationDate = storageItemEntity.expirationDate
                    )
                )
            )
        }
}



    suspend fun searchProductsByLocationId(locationId: String): LocationProductsResponse {
        val skladId = spHelper.getSkladId()

        return if (networkUtils.isNetworkAvailable()) {
            // If network is available, make the API request
            api.getLocationProducts(locationId = locationId, skladId = skladId.toInt())
        } else {
            // If no network, query from the local database
            Log.d("ProductSearchRepository", "Нет интернета, ищем в локальной базе данных по locationId")
            val localData = searchLocationProductsFromDatabase(locationId)
            // Assuming you want to return a response in the same format as the API response
            LocationProductsResponse(success = true, data = localData, message = null)
        }
    }

    suspend fun searchProductsByLocationName(locationName: String): LocationProductsResponse {
        val skladId = spHelper.getSkladId()

        return if (networkUtils.isNetworkAvailable()) {
            // If network is available, make the API request
            api.getLocationProducts(locationId = locationName, skladId = skladId.toInt())
        } else {
            // If no network, query from the local database
            Log.d("ProductSearchRepository", "Нет интернета, ищем в локальной базе данных по locationName")
            val localData = searchLocationProductsFromDatabase(locationName)
            // Assuming you want to return a response in the same format as the API response
            LocationProductsResponse(success = true, data = localData, message = null)
        }
    }

    suspend fun searchProductsByArticle(article: String): ProductInfoResponse {
        val skladId = spHelper.getSkladId()

        return if (networkUtils.isNetworkAvailable()) {
            // If network is available, make the API request
            api.getProductInfo(article = article, skladId = skladId.toInt())
        } else {
            // If no network, query from the local database
            Log.d("ProductSearchRepository", "Нет интернета, ищем в локальной базе данных по article")
            val localData = searchProductInfoFromDatabase(article)
            // Assuming you want to return a response in the same format as the API response
            ProductInfoResponse(success = true, data = localData, message = null)
        }
    }

    suspend fun searchProductsByBarcode(barcode: String): ProductInfoResponse {
        val skladId = spHelper.getSkladId()

        return if (networkUtils.isNetworkAvailable()) {
            // If network is available, make the API request
            api.getProductInfo(shk = barcode, skladId = skladId.toInt())
        } else {
            // If no network, query from the local database
            Log.d("ProductSearchRepository", "Нет интернета, ищем в локальной базе данных по barcode")
            val localData = searchProductInfoFromDatabase(barcode)
            // Assuming you want to return a response in the same format as the API response
            ProductInfoResponse(success = true, data = localData, message = null)
        }
    }

    private suspend fun searchLocationProductsFromDatabase(locationId: String): List<LocationProduct> {
        // Query from the database by locationId
        val storageItems = dao.findItemsByShk(locationId)
        return storageItems.map { storageItemEntity ->
            LocationProduct(
                id = storageItemEntity.id,
                locationId = storageItemEntity.wrShk, // Example, adjust if needed
                quantity = storageItemEntity.placeQnt,
                conditionState = storageItemEntity.conditionState,
                expirationDate = storageItemEntity.expirationDate,
                shk = storageItemEntity.shk,
                name = storageItemEntity.name,
                article = storageItemEntity.article
            )
        }
    }

    private suspend fun searchProductInfoFromDatabase(query: String): ProductInfo {
        // Query from the database by article or barcode
        val storageItems = if (query.length > 7) {
            dao.findItemsByShk(query)
        } else {
            dao.findItemsByArticle(query)
        }

        // If the list is empty, return a default ProductInfo or throw a custom exception
        if (storageItems.isEmpty()) {
            Log.e("ProductSearchRepository", "No products found for query: $query")
            throw NoSuchElementException("No products found for query: $query")
        }

        // Now it's safe to access the first item
        return ProductInfo(
            id = storageItems.first().id,
            name = storageItems.first().name,
            shk = storageItems.first().shk,
            article = storageItems.first().article,
            totalQuantity = storageItems.first().productQnt,
            items = storageItems.map { storageItemEntity ->
                ProductLocation(
                    id = storageItemEntity.id,
                    name_wr = storageItemEntity.wrShk,
                    quantity = storageItemEntity.placeQnt,
                    conditionState = storageItemEntity.conditionState,
                    expirationDate = storageItemEntity.expirationDate
                )
            }
        )
    }



}