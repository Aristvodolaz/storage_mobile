package com.komus.sorage_mobile.domain.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExpirationDateValidator {
    private const val TAG = "ExpirationDateValidator"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Проверяет, истек ли срок годности
     * @param expirationDate дата в формате yyyy-MM-dd
     * @return true если срок годности истек, false в противном случае
     */
    fun isExpired(expirationDate: String): Boolean {
        return try {
            val expiration = dateFormat.parse(expirationDate)
            val today = Date()
            expiration?.before(today) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке срока годности: ${e.message}")
            false
        }
    }

    /**
     * Проверяет, можно ли установить состояние "Кондиция" для товара с указанным сроком годности
     * @param expirationDate дата в формате yyyy-MM-dd
     * @return true если можно установить состояние "Кондиция", false если срок годности истек
     */
    fun canSetGoodCondition(expirationDate: String): Boolean {
        return !isExpired(expirationDate)
    }

    /**
     * Проверяет валидность срока годности для указанного состояния
     * @param expirationDate дата в формате yyyy-MM-dd
     * @param condition состояние товара ("Кондиция" или "Некондиция")
     * @return true если срок годности валиден для указанного состояния, false в противном случае
     */
    fun isValidForCondition(expirationDate: String, condition: String): Boolean {
        val isExpired = isExpired(expirationDate)
        return when (condition) {
            "Кондиция" -> !isExpired
            "Некондиция" -> true // Для некондиции срок годности может быть любым
            else -> false
        }
    }
} 