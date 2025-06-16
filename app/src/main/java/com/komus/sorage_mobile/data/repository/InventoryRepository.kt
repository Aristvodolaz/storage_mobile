package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.api.InventoryRequest
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.entity.InventoryEntity
import com.komus.sorage_mobile.domain.model.InventoryItem
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import javax.inject.Inject
import android.util.Log
import com.komus.sorage_mobile.util.DateUtils.convertToIsoFormat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val api: StorageApi,
    private val dao: InventoryDao,
    private val spHelper: SPHelper,
    private val networkUtils: NetworkUtils
) {
    companion object {
        private const val TAG = "InventoryRepository"
    }

    /**
     * Получение списка товаров по ID ячейки
     */
    suspend fun getItemsByLocationId(locationId: String): List<InventoryItem> {
        return if (true) {
            try {
                // Пытаемся получить данные с сервера
                val skladId = spHelper.getSkladId()
                val response = api.getLocationItems(locationId, skladId.toInt())

                if (!response.success) {
                    throw Exception(response.message ?: "Ошибка при получении данных")
                }

                val items = response.data.map { product ->
                    val actualQuantity = product.units[0].quantity.toInt()
                    Log.d(TAG, "Получены данные с сервера: артикул=${product.article}, кол-во=${actualQuantity}")
                    
                    InventoryItem(
                        id = product.id.toString(),
                        name = product.name,
                        article = product.article,
                        barcode = product.shk,
                        locationId = product.idSklad.toString(),
                        locationName = "",
                        expectedQuantity = actualQuantity,
                        actualQuantity = actualQuantity,
                        isChecked = false,
                        expirationDate = product.units[0].expirationDate ?: "",
                        condition = product.units[0].conditionState ,
                        reason = null
                    )
                }

                // Сохраняем полученные данные в локальную базу
                withContext(Dispatchers.IO) {
                    dao.deleteItemsByLocationId(locationId)
                    dao.insertItems(items.map {
                        InventoryEntity.fromInventoryItem(it, spHelper.getUserName(), true)
                    })
                }

                items
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении данных с сервера: ${e.message}")
                // В случае ошибки, возвращаем данные из локальной базы
                dao.getItemsByLocationId(locationId).first().map { 
                    val item = it.toInventoryItem()
                    Log.d(TAG, "Получены данные из БД: артикул=${item.article}, кол-во=${item.actualQuantity}")
                    item 
                }
            }
        } else {
            // Если нет сети, возвращаем данные из локальной базы
            dao.getItemsByLocationId(locationId).first().map { 
                val item = it.toInventoryItem()
                Log.d(TAG, "Нет сети. Получены данные из БД: артикул=${item.article}, кол-во=${item.actualQuantity}")
                item 
            }
        }
    }

    /**
     * Получение списка товаров по артикулу
     */
    suspend fun getIgetItemsByArticletemsBySku(sku: String): List<InventoryItem> {
        return if (true) {
            try {
                val skladId = spHelper.getSkladId()
                val response = api.getInventoryItemByArticle(sku, skladId)

                if (!response.success) {
                    throw Exception("Ошибка при получении данных")
                }

                val items = response.data.items.map { item ->
                    InventoryItem(
                        id = item.id.toString(),
                        name = item.name,
                        article = item.article,
                        barcode = item.shk,
                        locationId = item.wrShk,  // ✅ Используйте wrShk вместо idScklad
                        locationName = item.prunitName,
                        expectedQuantity = item.productQnt,
                        actualQuantity = item.placeQnt,
                        isChecked = false,
                        expirationDate = item.expirationDate ?: "",
                        condition =item.conditionState ,
                        reason = null
                    )
                }

                withContext(Dispatchers.IO) {
                    dao.insertItems(items.map {
                        InventoryEntity.fromInventoryItem(it, spHelper.getUserName(), true)
                    })
                }

                items
            } catch (e: Exception) {
                Log.e("InventoryRepository", "Ошибка при получении данных с сервера: ${e.message}")
                dao.getItemsByArticle(sku).first().map { it.toInventoryItem() }
            }
        } else {
            dao.getItemsByArticle(sku).first().map { it.toInventoryItem() }
        }
    }


    suspend fun updateItemQuantity(item: InventoryItem, newQuantity: Int): InventoryItem {
        // В реальном приложении здесь будет запрос к API
        // Для прототипа просто возвращаем обновленный объект
        return item.copy(actualQuantity = newQuantity, isChecked = true)
    }

    /**
     * Подтверждение товара без изменения количества
     */
    suspend fun confirmItem(item: InventoryItem): InventoryItem {
        val userName = spHelper.getUserName()
        val inventoryEntity = InventoryEntity.fromInventoryItem(item, userName, false)

        // Сохраняем в локальную базу
        dao.insertItem(inventoryEntity)

        // Если есть сеть, пытаемся синхронизировать
        if (true) {
            try {
                val request = InventoryRequest(
                    id = item.id,
                    quantity = item.actualQuantity,
                    expirationDate = convertToIsoFormat(item.expirationDate),
                    conditionState = if (item.condition == "Кондиция") "кондиция" else "некондиция",
                    reason = item.reason ?: "",
                    executor = userName
                )

                val response = api.confirmInventoryItem(request)

                if (response.success) {
                    dao.markAsSynced(item.id.toLong())
                }
            } catch (e: Exception) {
                Log.e("InventoryRepository", "Ошибка при синхронизации: ${e.message}")
                // Ошибка синхронизации не должна прерывать работу пользователя
            }
        }

        return item.copy(isChecked = true)
    }

    /**
     * Обновление данных товара
     */
    suspend fun updateItem(
        item: InventoryItem,
        newQuantity: Int,
        newExpirationDate: String,
        newCondition: String,
        newReason: String?
    ): InventoryItem {
        val userName = spHelper.getUserName()
        val updatedItem = item.copy(
            actualQuantity = newQuantity,
            expirationDate = newExpirationDate,
            condition = newCondition,
            reason = newReason,
            isChecked = true
        )

        // Сохраняем в локальную базу
        val inventoryEntity = InventoryEntity.fromInventoryItem(updatedItem, userName, false)
        dao.insertItem(inventoryEntity)

        // Если есть сеть, пытаемся синхронизировать
        if (true) {
            try {
                val request = InventoryRequest(
                    id = item.id,
                    quantity = newQuantity,
                    expirationDate = newExpirationDate,
                    conditionState = if (newCondition == "Кондиция") "кондиция" else "некондиция",
                    reason = newReason ?: "",
                    executor = userName
                )

                val response = api.confirmInventoryItem(request)

                if (response.success) {
                    dao.markAsSynced(item.id.toLong())
                }
            } catch (e: Exception) {
                Log.e("InventoryRepository", "Ошибка при синхронизации: ${e.message}")
                // Ошибка синхронизации не должна прерывать работу пользователя
            }
        }

        return updatedItem
    }

    /**
     * Синхронизация несинхронизированных данных
     */
    suspend fun syncUnsyncedItems() {
//        if (!networkUtils.isNetworkAvailable()) return

        val unsyncedItems = dao.getUnsyncedItems()
        val userName = spHelper.getUserName()

        unsyncedItems.forEach { entity ->
            try {
                val request = InventoryRequest(
                    id = entity.id.toString(),
                    quantity = entity.quantity,
                    expirationDate = entity.expirationDate,
                    conditionState = if (entity.condition == "Кондиция") "кондиция" else "некондиция",
                    reason = entity.reason ?: "",
                    executor = userName
                )

                val response = api.confirmInventoryItem(request)

                if (response.success) {
                    dao.markAsSynced(entity.id.toLong())
                }
            } catch (e: Exception) {
                Log.e(
                    "InventoryRepository",
                    "Ошибка при синхронизации item ${entity.id}: ${e.message}"
                )
            }
        }
    }


    /**
     * Очистка синхронизированных данных
     */
    suspend fun clearSyncedData() {
        dao.deleteSyncedItems()
    }

    /**
     * Проверка наличия несинхронизированных изменений
     */
    fun hasUnsyncedChanges(): Flow<Boolean> {
        return dao.getUnsyncedItemsFlow()
            .map { items -> items.isNotEmpty() }
            .distinctUntilChanged()
    }


    suspend fun syncInventory() {
//        if (!networkUtils.isNetworkAvailable()) {
//            Log.d(TAG, "Network is not available, skipping sync")
//            return
//        }

        try {
            Log.d(TAG, "Starting inventory sync")
            val unsyncedItems = dao.getUnsyncedItems()
            Log.d(TAG, "Found ${unsyncedItems.size} unsynced items")

            unsyncedItems.forEach { item ->
                try {
                    val request = InventoryRequest(
                        id = item.id.toString(),
                        quantity = item.quantity,
                        expirationDate = item.expirationDate,
                        conditionState = if (item.condition == "Кондиция") "кондиция" else "некондиция",
                        reason = item.reason ?: "",
                        executor = spHelper.getUserName()
                    )

                    val response = api.confirmInventoryItem(request)

                    if (response.success) {
                        dao.markAsSynced(item.id.toLong())
                        Log.d(TAG, "Successfully synced item ${item.id}")
                    } else {
                        Log.e(TAG, "Failed to sync item ${item.id}: ${response.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync item ${item.id}", e)
                }
            }

            Log.d(TAG, "Inventory sync completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during inventory sync", e)
            throw e
        }
    }

    suspend fun syncPendingInventory() {
//        if (!networkUtils.isNetworkAvailable()) {
//            Log.d("InventoryRepository", "Нет подключения к интернету для синхронизации инвентаризации")
//            return
//        }
//
        try {
            // Получаем несинхронизированные данные инвентаризации
            val unsyncedInventory = dao.getUnsyncedInventoryItems()
            if (unsyncedInventory.isEmpty()) {
                Log.d("InventoryRepository", "Нет данных инвентаризации для синхронизации")
                return
            }
            
            Log.d("InventoryRepository", "Найдено ${unsyncedInventory.size} несинхронизированных записей инвентаризации")
            
            for (item in unsyncedInventory) {
                try {
                    // Отправляем данные инвентаризации на сервер
                    val request = InventoryRequest(
                        id = item.id,
                        quantity = item.quantity,
                        expirationDate = convertToIsoFormat(item.expirationDate),
                        conditionState = if (item.condition == "Кондиция") "кондиция" else "некондиция",
                        reason = item.reason,
                        executor = spHelper.getUserName()
                    )
                    
                    // Здесь нужно адаптировать под реальное API
                    val response = api.confirmInventoryItem(request)
                    
                    // Проверяем успешность ответа (адаптируйте под реальный ответ API)
                    val isSuccess = response != null
                    
                    if (isSuccess) {
                        // Помечаем как синхронизированные
                        dao.markInventoryAsSynced(item.id)
                        Log.d("InventoryRepository", "Инвентаризация синхронизирована: ${item.id}")
                    } else {
                        Log.e("InventoryRepository", "Ошибка при синхронизации инвентаризации")
                    }
                } catch (e: Exception) {
                    Log.e("InventoryRepository", "Ошибка при отправке данных инвентаризации: ${item.id}", e)
                    // Продолжаем с следующей записью
                }
            }
            
            Log.d("InventoryRepository", "Синхронизация инвентаризации завершена")
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Ошибка при синхронизации инвентаризации", e)
            throw e
        }
    }
}