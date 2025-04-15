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

            // Fetch response from the API
            val response: StorageResponse = api.getAllStorageItems()

            // Checking if the response is successful and if the items are available
            if (response.success && response.data.items.isNotEmpty()) {

                dao.clearStorageItems()

                val items = response.data.items.map { dto ->
                    val itemName = dto.name ?: "Неизвестное название"
                    StorageItemEntity(
                        id = dto.id,
                        name = itemName,
                        article = dto.article,
                        shk = dto.shk?: "",
                        productQnt = dto.productQnt,
                        placeQnt = dto.placeQnt,
                        prunitId = dto.prunitId,
                        prunitName = dto.prunitName,
                        wrShk = dto.wrShk,
                        idScklad = dto.idScklad,
                        conditionState = dto.conditionState,
                        expirationDate = dto.expirationDate ?: "", // Если expirationDate равно null, используем пустую строку
                        createDate = dto.createDate,
                        updateDate = dto.updateDate ?: "", // Если updateDate равно null, используем пустую строку
                        executor = dto.executor,
                        name_wr = dto.name_wr?: ""
                    )
                }


                // Insert the mapped items into the database
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


    suspend fun findItemByArticleOrBarcode(query: String): List<StorageItemEntity> {
        Log.d(TAG, "Поиск товара по запросу: $query")
        try {
            // Сначала ищем локально
            val localItems = dao.searchStorageItems("%$query%")
            Log.d(TAG, "Найдено ${localItems.size} товаров локально")
            
            // Если есть сеть, пытаемся найти на сервере
            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = api.searchProductByArticleOrBarcode(query)
                    
                    if (response.success && response.data.items.isNotEmpty()) {
                        Log.d(TAG, "Найдено ${response.data.items.size} товаров на сервере")
                        
                        val serverItems = response.data.items.map { dto ->
                            val itemName = dto.name ?: "Неизвестное название"
                            StorageItemEntity(
                                id = dto.id,
                                name = itemName,
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
                        val allItems = (localItems + serverItems).distinctBy { it.id }
                        Log.d(TAG, "Общее количество найденных товаров: ${allItems.size}")
                        return allItems
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при поиске товара на сервере", e)
                    // Если ошибка при поиске на сервере, возвращаем только локальные результаты
                }
            }
            
            return localItems
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при поиске товара", e)
            throw e
        }
    }

    // Упрощенный метод для обратной совместимости
    suspend fun findItemByArticleOrBarcode(article: String, barcode: String): StorageItemEntity? {
        val query = if (article.isNotEmpty()) article else barcode
        val results = findItemByArticleOrBarcode(query)
        return results.firstOrNull()
    }

} 