package com.komus.sorage_mobile.domain.viewModel

import android.os.Build
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
        return try {
            val initialDate = LocalDate.parse(startDate, inputFormatter)
            val finalDate = initialDate.plusDays(days.toLongOrNull() ?: 0).plusMonths(months.toLongOrNull() ?: 0)
            finalDate.format(outputFormatter)
        } catch (e: Exception) {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveExpirationData(startDate: String, days: String, months: String, condition: String) {
        viewModelScope.launch {
            val endDate = calculateEndDate(startDate, days, months)
            spHelper.saveSrokGodnosti(endDate)
            spHelper.saveCondition(condition)
        }
    }
}