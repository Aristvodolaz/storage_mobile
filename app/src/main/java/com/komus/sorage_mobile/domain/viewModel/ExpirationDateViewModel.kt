package com.komus.sorage_mobile.domain.viewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    private val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

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
            Log.e("ExpirationDateViewModel", "Ошибка расчета даты: ${e.message}")
            return ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveExpirationData(startDate: String, days: String, months: String, condition: String) {
        viewModelScope.launch {
            val endDate = calculateEndDate(startDate, days, months)
            Log.d("ExpirationDateViewModel", "Сохраняем срок годности: $endDate")
            spHelper.saveSrokGodnosti(endDate)
            
            Log.d("ExpirationDateViewModel", "Сохраняем состояние товара: $condition")
            spHelper.saveCondition(condition)
        }
    }
}