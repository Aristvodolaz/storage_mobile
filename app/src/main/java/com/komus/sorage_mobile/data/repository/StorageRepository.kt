package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.entity.StorageItemEntity
import com.komus.sorage_mobile.util.NetworkUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.komus.sorage_mobile.data.model.StorageResponse
import com.komus.sorage_mobile.data.response.ProductInfoResponse

@Singleton
class StorageRepository @Inject constructor(
    private val api: StorageApi,
    private val dao: InventoryDao,
    private val networkUtils: NetworkUtils
) {
    companion object {
        private const val TAG = "StorageRepository"
    }

    suspend fun syncStorageItems() {
        if (!networkUtils.isNetworkAvailable()) {
            Log.d(TAG, "Нет подключения к интернету для синхронизации storage items")
            return
        }

        try {
            Log.d(TAG, "Начало синхронизации storage items")
            val response = api.getAllStorageItems()

            if (response.success && response.data.items.isNotEmpty()) {
                dao.clearStorageItems()

                val items = response.data.items.map { dto ->
                    StorageItemEntity(
                        id = dto.id,
                        name = dto.name ?: "Неизвестное название",
                        article = dto.article,
                        shk = dto.shk ?: "",
                        productQnt = dto.productQnt,
                        placeQnt = dto.placeQnt,
                        prunitId = dto.prunitId,
                        prunitName = dto.prunitName,
                        wrShk = dto.wrShk,
                        idScklad = dto.idScklad,
                        conditionState = dto.conditionState,
                        expirationDate = dto.expirationDate ?: "",
                        createDate = dto.createDate,
                        updateDate = dto.updateDate ?: "",
                        executor = dto.executor,
                        name_wr = dto.name_wr ?: ""
                    )
                }

                dao.insertStorageItems(items)
                Log.d(TAG, "Синхронизация storage items завершена успешно. Сохранено ${items.size} записей")
            } else {
                Log.e(TAG, "Ошибка при синхронизации storage items: пустой ответ от сервера или ошибка в ответе")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при синхронизации storage items", e)
            throw e
        }
    }

    // Поиск по локальной базе данных
    suspend fun searchLocalItems(query: String): List<StorageItemEntity> {
        return dao.searchStorageItems("%$query%")
    }

    // Поиск товара по артикулу или штрих-коду с указанием склада
//    suspend fun searchProductByArticleOrBarcode(
//        article: String? = null,
//        shk: String? = null,
//    ): Result<ProductInfoResponse> {
//        return try {
//            val response = api.searchProductByArticleOrBarcode(article, shk)
//            if (response.isSuccessful && response.body() != null) {
//                Result.success(response.body()!!)
//            } else {
//                Result.failure(Exception("Failed to get product info: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    // Поиск товара по общему запросу (для обратной совместимости)
    suspend fun searchProductByQuery(query: String): List<StorageItemEntity> {
        Log.d(TAG, "Поиск товара по запросу: $query")
        try {
            // Сначала ищем локально
            val localItems = searchLocalItems(query)
            Log.d(TAG, "Найдено ${localItems.size} товаров локально")

            // Если есть сеть, пытаемся найти на сервере
            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = api.searchProductByArticleOrBarcode(query)

                    if (response.success && response.data.items.isNotEmpty()) {
                        Log.d(TAG, "Найдено ${response.data.items.size} товаров на сервере")

                        val serverItems = response.data.items.map { dto ->
                            StorageItemEntity(
                                id = dto.id,
                                name = dto.name ?: "Неизвестное название",
                                article = dto.article,
                                shk = dto.shk ?: "",
                                productQnt = dto.productQnt,
                                placeQnt = dto.placeQnt,
                                prunitId = dto.prunitId,
                                prunitName = dto.prunitName,
                                wrShk = dto.wrShk,
                                idScklad = dto.idScklad,
                                conditionState = dto.conditionState,
                                expirationDate = dto.expirationDate ?: "",
                                createDate = dto.createDate,
                                updateDate = dto.updateDate ?: "",
                                executor = dto.executor,
                                name_wr = dto.name_wr ?: ""
                            )
                        }

                        // Сохраняем найденные товары в базу
                        dao.insertStorageItems(serverItems)

                        // Возвращаем объединенные результаты без дубликатов
                        return (localItems + serverItems).distinctBy { it.id }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при поиске товара на сервере", e)
                }
            }

            return localItems
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при поиске товара", e)
            throw e
        }
    }
}

// Модели для отображения
data class ProductDisplayItem(
    val id: Long,
    val name: String,
    val article: String,
    val shk: String,
    val quantity: Int,
    val location: String,
    val conditionState: String,
    val expirationDate: String
)

data class LocationWithItems(
    val locationId: String,
    val locationName: String,
    val items: List<ProductDisplayItem>
) 