package com.komus.sorage_mobile.domain.viewModel

import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.komus.sorage_mobile.data.placement.PlacementDraftStore
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/** Срок «без ограничения» хранится как 2999-01-01, как и раньше. */
const val NO_EXPIRATION_ISO = "2999-01-01"

@HiltViewModel
class ExpirationDateViewModel @Inject constructor(
    private val draftStore: PlacementDraftStore
) : ViewModel() {

    private val displayFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /** Дата окончания срока в формате дд.мм.гггг или пустая строка, если данных недостаточно или они некорректны. */
    @RequiresApi(android.os.Build.VERSION_CODES.O)
    fun calculateEndDate(startDate: String, days: String, months: String): String {
        val start = parseDisplayDate(startDate) ?: return ""
        if (days.isEmpty() && months.isEmpty()) return ""
        return try {
            var end = start
            if (days.isNotEmpty()) end = end.plusDays(days.toLong())
            if (months.isNotEmpty()) end = end.plusMonths(months.toLong())
            end.format(displayFormat)
        } catch (e: NumberFormatException) {
            ""
        }
    }

    /**
     * Проверяет данные шага срока годности и записывает их в черновик.
     * @return null, если всё верно и черновик обновлён; иначе текст ошибки для оператора.
     */
    @RequiresApi(android.os.Build.VERSION_CODES.O)
    fun save(
        skipExpiration: Boolean,
        startDate: String,
        days: String,
        months: String,
        condition: String,
        reason: String
    ): String? {
        if (condition == "Некондиция" && reason.isBlank()) {
            return "Укажите причину некондиции"
        }

        val expirationIso = if (skipExpiration) {
            NO_EXPIRATION_ISO
        } else {
            val endDisplay = calculateEndDate(startDate, days, months)
            if (parseDisplayDate(startDate) == null) {
                return "Введите дату производства в формате дд.мм.гггг"
            }
            if (endDisplay.isEmpty()) {
                return "Укажите срок хранения: дни или месяцы"
            }
            LocalDate.parse(endDisplay, displayFormat).toString()
        }

        if (condition == "Кондиция" && ExpirationDateValidator.isExpired(expirationIso)) {
            return "Нельзя установить «Кондиция» для товара с истекшим сроком годности"
        }

        draftStore.update {
            it.copy(
                expirationDate = expirationIso,
                condition = condition,
                reason = if (condition == "Некондиция") reason else ""
            )
        }
        return null
    }

    private fun parseDisplayDate(text: String): LocalDate? =
        try {
            if (text.length == 10) LocalDate.parse(text, displayFormat) else null
        } catch (e: DateTimeParseException) {
            null
        }
}
