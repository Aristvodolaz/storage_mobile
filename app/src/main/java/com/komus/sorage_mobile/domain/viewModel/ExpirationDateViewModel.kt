package com.komus.sorage_mobile.domain.viewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.domain.model.NonConformityReason
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import com.komus.sorage_mobile.util.DateUtils
import com.komus.sorage_mobile.util.ProductMovementHelper
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ExpirationDateViewModel @Inject constructor(
    private val spHelper: SPHelper
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateEndDate(startDate: String, days: String, months: String): String {
        return try {
            val start = LocalDate.parse(startDate, dateFormatter)
            var end = start

            if (days.isNotEmpty()) {
                end = end.plusDays(days.toLong())
            }
            
            if (months.isNotEmpty()) {
                end = end.plusMonths(months.toLong())
            }

            end.format(dateFormatter)
        } catch (e: Exception) {
            Log.e("ExpirationDateViewModel", "Ошибка расчета даты: ${e.message}")
            ""
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
            try {
                val localDate = calculateEndDate(startDate, days, months)
                if (localDate.isEmpty()) {
                    _errorMessage.value = "Ошибка расчета даты"
                    return@launch
                }
                
                Timber.d("Расчитанная дата окончания срока годности (локальный формат): $localDate")
                
                // Преобразуем в ISO формат перед сохранением
                val isoDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Используем LocalDate для Android 8.0+
                    val date = LocalDate.parse(localDate, dateFormatter)
                    date.format(DateTimeFormatter.ISO_DATE)
                } else {
                    // Используем DateUtils для старых версий Android
                    DateUtils.convertToIsoFormat(localDate)
                }
                
                if (isoDate.isEmpty()) {
                    _errorMessage.value = "Ошибка преобразования даты"
                    return@launch
                }
                
                Timber.d("ISO формат даты для сохранения: $isoDate")
                
                // Валидируем дату перед сохранением с помощью ProductMovementHelper
                val validatedIsoDate = ProductMovementHelper.processExpirationDate(isoDate)
                Timber.d("Итоговый ISO формат даты: $validatedIsoDate")

                // Проверяем валидность срока годности для указанного состояния
                // Разрешаем сохранение для некондиции даже с истекшим сроком
                if (ExpirationDateValidator.isExpired(validatedIsoDate) && condition == "Кондиция") {
                    _errorMessage.value = "Невозможно установить состояние 'Кондиция' для товара с истекшим сроком годности"
                    return@launch
                }
                
                // Проверяем, что для некондиции указана валидная причина
                if (condition == "Некондиция" && reason.isNullOrEmpty()) {
                        _errorMessage.value = "Для некондиции необходимо указать причину"
                        return@launch
                }

                Log.d("DATE", validatedIsoDate)
                Log.d("condition", condition)

                // Сохраняем все данные
                spHelper.saveSrokGodnosti(validatedIsoDate)
                spHelper.saveCondition(condition)
                
                // Сохраняем причину некондиции если она указана
                if (condition == "Некондиция" && !reason.isNullOrEmpty()) {
                    spHelper.saveReason(reason)
                }
                
                _errorMessage.value = null
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при сохранении данных о сроке годности")
                _errorMessage.value = "Ошибка при сохранении данных: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}