package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.data.repository.toProductInfo
import com.komus.sorage_mobile.data.response.LocationDetails
import com.komus.sorage_mobile.data.response.ProductItem
import com.komus.sorage_mobile.data.response.UnitWithLocations
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductMappingTest {

    private fun location(id: String, wrShk: String, quantity: Int) = LocationDetails(
        id = id,
        wrShk = wrShk,
        name = "ячейка $id",
        zone = "Z",
        rack = "R",
        shelf = "S",
        position = "P",
        quantity = quantity,
        conditionState = "Кондиция",
        expirationDate = "2027-01-01",
        createdAt = "",
        updatedAt = "",
        createdBy = "",
        updatedBy = ""
    )

    @Test
    fun mapsIdentityFieldsAndParsesNumericId() {
        val item = ProductItem(id = "42", name = "Бумага", article = "13500", shk = "4600", units = emptyMap())

        val info = item.toProductInfo()

        assertEquals(42, info.id)
        assertEquals("Бумага", info.name)
        assertEquals("13500", info.article)
        assertEquals("4600", info.shk)
    }

    @Test
    fun nonNumericIdBecomesZeroInsteadOfCrashing() {
        val item = ProductItem(id = "abc", name = "n", article = "a", shk = "s", units = emptyMap())

        assertEquals(0, item.toProductInfo().id)
    }

    @Test
    fun totalQuantitySumsAllLocationsOfAllUnits() {
        val item = ProductItem(
            id = "1", name = "n", article = "a", shk = "s",
            units = mapOf(
                "pcs" to UnitWithLocations(
                    id = "u1", name = "шт",
                    locations = listOf(location("7", "W1", 3), location("8", "W2", 4))
                ),
                "box" to UnitWithLocations(
                    id = "u2", name = "коробка",
                    locations = listOf(location("9", "W3", 5))
                )
            )
        )

        val info = item.toProductInfo()

        assertEquals(12, info.totalQuantity)
        assertEquals(3, info.items.size)
    }

    @Test
    fun locationFieldsAreCopiedToProductLocation() {
        val item = ProductItem(
            id = "1", name = "n", article = "a", shk = "s",
            units = mapOf("pcs" to UnitWithLocations("u1", "шт", listOf(location("7", "W1", 3))))
        )

        val loc = item.toProductInfo().items.single()

        assertEquals(7, loc.id)
        assertEquals("W1", loc.name_wr)
        assertEquals(3, loc.quantity)
        assertEquals("Кондиция", loc.conditionState)
        assertEquals("2027-01-01", loc.expirationDate)
    }

    @Test
    fun emptyUnitsGiveZeroTotalAndNoItems() {
        val info = ProductItem(id = "1", name = "n", article = "a", shk = "s", units = emptyMap()).toProductInfo()

        assertEquals(0, info.totalQuantity)
        assertEquals(0, info.items.size)
    }
}
