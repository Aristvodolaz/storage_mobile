package com.komus.sorage_mobile.util

import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Утилитарный класс для работы с датами в приложении
 */
object DateUtils {
    
    private const val TAG = "DateUtils"
    
    /**
     * Преобразует дату из формата dd.MM.yyyy в формат ISO yyyy-MM-dd
     */
    fun convertToIsoFormat(inputDate: String): String {
        if (inputDate.isEmpty()) return ""
        
        return try {
            // Поддержка нескольких входных форматов
            if (inputDate.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
                // Формат dd.MM.yyyy
                val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(inputDate)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    Timber.e("Не удалось распарсить дату: $inputDate")
                    ""
                }
            } else if (inputDate.matches(Regex("\\w+ \\d{1,2} \\d{4}.*"))) {
                // Формат "Month DD YYYY" (например, "Oct 11 2025  9:00PM")
                val inputFormat = SimpleDateFormat("MMM dd yyyy hh:mma", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(inputDate)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    Timber.e("Не удалось распарсить дату: $inputDate")
                    ""
                }
            } else if (inputDate.contains("-") && inputDate.split("-").size == 3) {
                // Формат уже может быть в yyyy-MM-dd
                if (inputDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    inputDate // Уже в нужном формате
                } else {
                    // Другие форматы с дефисами
                    val parts = inputDate.split("-")
                    val day = parts[0].toIntOrNull()
                    val month = parts[1].toIntOrNull()
                    val year = parts[2].toIntOrNull()
                    
                    if (day != null && month != null && year != null) {
                        String.format("%04d-%02d-%02d", year, month, day)
                    } else {
                        Timber.e("Неверный формат даты: $inputDate")
                        ""
                    }
                }
            } else {
                Timber.e("Неподдерживаемый формат даты: $inputDate")
                ""
            }
        } catch (e: ParseException) {
            Timber.e(e, "Ошибка при конвертации даты: $inputDate")
            ""
        }
    }
    
    /**
     * Преобразует дату из ISO формата yyyy-MM-dd в локальный формат dd.MM.yyyy
     */
    fun convertFromIsoFormat(isoDate: String): String {
        if (isoDate.isEmpty()) return ""
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(isoDate)
            if (date != null) {
                outputFormat.format(date)
            } else {
                Timber.e("Не удалось распарсить ISO дату: $isoDate")
                ""
            }
        } catch (e: ParseException) {
            Timber.e(e, "Ошибка при конвертации ISO даты: $isoDate")
            ""
        }
    }
    
    /**
     * Проверяет, является ли дата корректной в формате dd.MM.yyyy
     */
    fun isValidDate(date: String): Boolean {
        if (date.isEmpty()) return false
        
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: ParseException) {
            false
        }
    }
    
    /**
     * Возвращает текущую дату в формате dd.MM.yyyy
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * Возвращает текущую дату в формате ISO yyyy-MM-dd
     */
    fun getCurrentDateIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * Вычисляет дату, отстоящую от текущей на указанное количество дней
     * @param days количество дней (может быть отрицательным для прошедших дат)
     * @return дата в формате yyyy-MM-dd
     */
    fun getDateWithOffset(days: Int): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now()
            val result = today.plusDays(days.toLong())
            return result.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            // Для старых API
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(calendar.time)
        }
    }
} 