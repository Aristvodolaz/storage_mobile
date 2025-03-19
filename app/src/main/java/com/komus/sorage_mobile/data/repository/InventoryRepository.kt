package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.api.InventoryRequest
import com.komus.sorage_mobile.data.response.ProductData
import com.komus.sorage_mobile.domain.model.InventoryItem
import com.komus.sorage_mobile.util.SPHelper
import javax.inject.Inject
import android.util.Log

class InventoryRepository @Inject constructor(
    private val api: StorageApi,
    private val spHelper: SPHelper
) {
    /**
     * Получение списка товаров по ID ячейки
     */
    suspend fun getItemsByLocationId(locationId: String): List<InventoryItem> {
        val skladId = spHelper.getSkladId()
        val response = api.getLocationItems(locationId, skladId.toInt())
        
        if (!response.success) {
            throw Exception(response.message ?: "Ошибка при получении данных")
        }
        
        return response.data.map { product ->
            InventoryItem(
                id = product.id.toString(),
                name = product.name,
                article = product.article,
                barcode = product.shk,
                locationId = product.idSklad.toString(),
                locationName = "",  // В ответе нет названия ячейки
                expectedQuantity = product.units[0].quantity.toInt(),
                actualQuantity = product.units[0].quantity.toInt(),
                isChecked = false
            )
        }
    }
    
    /**
     * Получение списка товаров по артикулу
     */
    suspend fun getIgetItemsByArticletemsBySku(sku: String): List<InventoryItem> {
        val skladId = spHelper.getSkladId()
        val response = api.getInventoryItemByArticle( sku,  skladId)

        if (!response.success) {
            throw Exception("Ошибка при получении данных")
        }

        return response.data.items.map { item ->
            InventoryItem(
                id = item.id.toString(),
                name = item.name,
                article = item.article,  // Переименовано из article
                barcode = item.shk,
                locationId = item.idScklad.toString(),
                locationName = item.prunitName,
                expectedQuantity = item.productQnt,
                actualQuantity = item.placeQnt,
                isChecked = false
            )
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
        
        val request = InventoryRequest(
            id = item.id,
            quantity = item.actualQuantity,
            expirationDate = item.expirationDate,
            conditionState = if (item.condition == "Кондиция") "кондиция" else "некондиция",
            reason = item.reason ?: "",
            executor = userName
        )
        
        val response = api.confirmInventoryItem(request)
        
        if (!response.success) {
            throw Exception(response.message ?: "Ошибка при подтверждении товара")
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
        
        Log.d("InventoryRepository", "Обновление товара: id=${item.id}, количество=$newQuantity, срок годности=$newExpirationDate, состояние=$newCondition, причина=$newReason")
        
        val request = InventoryRequest(
            id = item.id,
            quantity = newQuantity,
            expirationDate = newExpirationDate,
            conditionState = if (newCondition == "Кондиция") "кондиция" else "некондиция",
            reason = newReason ?: "",
            executor = userName
        )
        
        val response = api.confirmInventoryItem(request)
        
        if (!response.success) {
            throw Exception(response.message ?: "Ошибка при обновлении товара")
        }
        
        return item.copy(
            actualQuantity = newQuantity,
            expirationDate = newExpirationDate,
            condition = newCondition,
            reason = newReason,
            isChecked = true
        )
    }
} 