package com.komus.sorage_mobile.domain.viewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.util.DateUtils
import com.komus.sorage_mobile.util.ProductMovementHelper
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExpirationDateViewModel @Inject constructor(
    private val spHelper: SPHelper
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    private val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateEndDate(startDate: String, days: String, months: String): String {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date = LocalDate.parse(startDate, formatter)
            
            var daysToAdd = 0
            if (days.isNotEmpty()) {
                daysToAdd = days.toInt()
            }
            
            var monthsToAdd = 0
            if (months.isNotEmpty()) {
                monthsToAdd = months.toInt()
            }
            
            val resultDate = date.plusDays(daysToAdd.toLong()).plusMonths(monthsToAdd.toLong())
            return resultDate.format(formatter)
        } catch (e: Exception) {
            Timber.e("Ошибка расчета даты: ${e.message}")
            return ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveExpirationData(
        startDate: String,
        days: String,
        months: String,
        condition: String,
        reason: String? = null
    ) {
        viewModelScope.launch {
            val localDate = calculateEndDate(startDate, days, months)
            Timber.d("Расчитанная дата окончания срока годности (локальный формат): $localDate")
            
            // Преобразуем в ISO формат перед сохранением
            val isoDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Используем LocalDate для Android 8.0+
                val date = LocalDate.parse(localDate, inputFormatter)
                date.format(outputFormatter)
            } else {
                // Используем DateUtils для старых версий Android
                DateUtils.convertToIsoFormat(localDate)
            }
            
            Timber.d("ISO формат даты для сохранения: $isoDate")
            
            // Валидируем дату перед сохранением с помощью ProductMovementHelper
            val validatedIsoDate = ProductMovementHelper.processExpirationDate(isoDate)
            Timber.d("Итоговый ISO формат даты: $validatedIsoDate")
            
            spHelper.saveSrokGodnosti(validatedIsoDate)
            spHelper.saveCondition(condition)
            
            // Сохраняем причину некондиции, если она указана
            if (condition == "Некондиция" && !reason.isNullOrEmpty()) {
                spHelper.saveReason(reason)
            } else {
                spHelper.saveReason("")
            }
        }
    }
    
    /**
     * Преобразует дату из локального формата dd.MM.yyyy в ISO формат yyyy-MM-dd
     * Этот метод необходим для совместимости со старыми API, не поддерживающими LocalDate
     */
    private fun convertToIsoFormat(localDate: String): String {
        return DateUtils.convertToIsoFormat(localDate)
    }
}