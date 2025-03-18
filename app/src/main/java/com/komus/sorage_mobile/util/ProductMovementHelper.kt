package com.komus.sorage_mobile.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Вспомогательный класс для операций перемещения товаров
 */
object ProductMovementHelper {
    
    private const val TAG = "ProductMovementHelper"
    
    /**
     * Значение даты срока годности по умолчанию в ISO формате (yyyy-MM-dd)
     */
    const val DEFAULT_EXPIRATION_DATE = "2025-01-01"
    
    /**
     * Обрабатывает дату срока годности перед отправкой на сервер,
     * преобразуя ее в ISO формат или возвращая значение по умолчанию при необходимости
     * 
     * @param expirationDate дата срока годности в любом поддерживаемом формате
     * @return дата в формате ISO (yyyy-MM-dd) или значение по умолчанию, если дата не валидна
     */
    fun processExpirationDate(expirationDate: String?): String {
        if (expirationDate.isNullOrEmpty()) {
            Timber.d("Использую дату по умолчанию: $DEFAULT_EXPIRATION_DATE")
            return DEFAULT_EXPIRATION_DATE
        }
        
        val isoDate = DateUtils.convertToIsoFormat(expirationDate)
        if (isoDate.isEmpty()) {
            Timber.w("Не удалось преобразовать дату '$expirationDate' в ISO формат, использую дату по умолчанию")
            return DEFAULT_EXPIRATION_DATE
        }
        
        Timber.d("Преобразована дата: '$expirationDate' -> '$isoDate'")
        return isoDate
    }
    
    /**
     * Проверяет формат даты и преобразует в ISO формат
     * 
     * @param date дата для проверки
     * @return true если дата была успешно преобразована, false в противном случае
     */
    fun validateAndConvertDate(date: String?): Boolean {
        if (date.isNullOrEmpty()) return false
        
        return DateUtils.convertToIsoFormat(date).isNotEmpty()
    }
    
    /**
     * Безопасное получение текущей даты в формате ISO
     */
    fun getCurrentIsoDate(): String {
        return try {
            DateUtils.getCurrentDateIso()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка получения текущей даты в ISO формате")
            DEFAULT_EXPIRATION_DATE
        }
    }
} 