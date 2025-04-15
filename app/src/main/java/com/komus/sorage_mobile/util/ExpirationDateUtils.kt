package com.komus.sorage_mobile.util

import java.text.SimpleDateFormat
import java.util.*

object ExpirationDateUtils {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun isExpired(expirationDateStr: String): Boolean {
        return try {
            if (expirationDateStr.isBlank()) return false
            
            val expirationDate = dateFormat.parse(expirationDateStr)
            val currentDate = Calendar.getInstance().time
            
            expirationDate?.before(currentDate) ?: false
        } catch (e: Exception) {
            false // В случае ошибки парсинга даты считаем, что срок не истек
        }
    }

    fun isValidExpirationDate(expirationDateStr: String): Boolean {
        return try {
            if (expirationDateStr.isBlank()) return false
            
            val expirationDate = dateFormat.parse(expirationDateStr)
            expirationDate != null
        } catch (e: Exception) {
            false
        }
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
} 