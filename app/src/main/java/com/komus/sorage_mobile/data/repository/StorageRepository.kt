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


    suspend fun findItemByArticleOrBarcode(article: String, barcode: String): StorageItemEntity? {
        return dao.findStorageItemByArticleOrBarcode(article, barcode)
    }

} 