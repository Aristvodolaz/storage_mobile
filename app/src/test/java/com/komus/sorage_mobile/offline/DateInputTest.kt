package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.presentation.screens.razmechenie.formatDateInput
import org.junit.Assert.assertEquals
import org.junit.Test

class DateInputTest {
    @Test
    fun insertsDotsAsDigitsAreTyped() {
        assertEquals("", formatDateInput(""))
        assertEquals("31", formatDateInput("31"))
        assertEquals("31.12", formatDateInput("3112"))
        assertEquals("31.12.2025", formatDateInput("31122025"))
    }

    @Test
    fun ignoresNonDigitsAndLimitsLength() {
        assertEquals("31.12.2025", formatDateInput("31.12.2025999"))
        assertEquals("01.2", formatDateInput("01.a2"))
    }
}
