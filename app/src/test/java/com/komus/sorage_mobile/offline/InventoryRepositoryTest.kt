package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.entity.InventoryEntity
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.response.ItemUnit
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.lang.reflect.Proxy

/**
 * Проверяет, что обновление ячейки с сервера не трогает несинхронизированные правки инвентаризации.
 * DAO собран через Proxy: все его методы возвращают значения без checked-исключений.
 */
class InventoryRepositoryTest {

    private val rows = mutableListOf<InventoryEntity>()
    private lateinit var api: FakeStorageApi
    private lateinit var repo: InventoryRepository

    private val dao: InventoryDao = Proxy.newProxyInstance(
        InventoryDao::class.java.classLoader,
        arrayOf(InventoryDao::class.java)
    ) { _, method, args ->
        when (method.name) {
            "getUnsyncedItems" -> rows.filter { !it.isSynced }
            "getItemsByLocationId" -> flowOf(rows.filter { it.cellId == args[0] })
            "getItemsByArticle" -> flowOf(rows.filter { it.article == args[0] })
            "deleteSyncedItemsByLocationId" -> {
                val before = rows.size
                rows.removeAll { it.cellId == args[0] && it.isSynced }
                before - rows.size
            }
            "insertItems" -> {
                @Suppress("UNCHECKED_CAST")
                val items = args[0] as List<InventoryEntity>
                items.forEach { item ->
                    rows.removeAll { it.id == item.id }
                    rows.add(item)
                }
                Unit
            }
            else -> throw UnsupportedOperationException("DAO-метод не используется в тесте: ${method.name}")
        }
    } as InventoryDao

    private fun entity(id: String, cell: String, synced: Boolean, qty: Int) = InventoryEntity(
        id = id,
        barcode = "SHK$id",
        article = "ART$id",
        name = "Товар $id",
        quantity = qty,
        cellBarcode = "",
        cellId = cell,
        product_id = 0,
        condition = "Кондиция",
        reason = "",
        expirationDate = "2027-01-01",
        isSynced = synced
    )

    @Before
    fun setUp() {
        rows.clear()
        rows.add(entity("u1", "C1", synced = false, qty = 9))   // несинхронизированная правка
        rows.add(entity("s1", "C1", synced = true, qty = 1))    // уже отправленная запись той же ячейки
        rows.add(entity("x1", "C2", synced = true, qty = 4))    // другая ячейка
        api = FakeStorageApi()
        repo = InventoryRepository(api, dao, FakeSession(userName = "user", skladId = "1383"))
    }

    private fun serverCell(c1: String, product: Int, qty: String) = LocationItemsResponse(
        success = true,
        data = listOf(
            LocationItem(
                id = product,
                name = "Серверный $product",
                article = "ART$product",
                shk = "SHK$product",
                idSklad = 1383,
                wrShk = "W1",
                units = listOf(
                    ItemUnit(
                        prunitId = 10,
                        prunitName = "шт",
                        quantity = qty,
                        conditionState = "Кондиция",
                        expirationDate = "2027-01-01",
                        productQnt = "1",
                        name_wr_shk = "W1"
                    )
                )
            )
        )
    )

    @Test
    fun onlineRefresh_keepsUnsyncedLocalChanges() = runBlocking {
        api.onLocationItems = { serverCell(it, product = 2, qty = "5") }

        val result = repo.getItemsByLocationId("C1")

        val unsynced = rows.single { it.id == "u1" }
        assertFalse(unsynced.isSynced)
        assertEquals(9, unsynced.quantity)
        assertTrue(result.any { it.id == "u1" })
    }

    @Test
    fun onlineRefresh_replacesOnlySyncedRowsOfThatCell() = runBlocking {
        api.onLocationItems = { serverCell(it, product = 2, qty = "5") }

        repo.getItemsByLocationId("C1")

        assertTrue(rows.none { it.id == "s1" })           // синхронизированная строка заменена сервером
        assertTrue(rows.any { it.id == "2" && it.isSynced })
        assertTrue(rows.any { it.id == "x1" })            // другая ячейка не тронута
    }

    @Test
    fun onlineRefresh_returnsLocalStateIncludingUnsynced() = runBlocking {
        api.onLocationItems = { serverCell(it, product = 2, qty = "5") }

        val result = repo.getItemsByLocationId("C1").map { it.id }.toSet()

        assertEquals(setOf("u1", "2"), result)
    }

    @Test
    fun offline_returnsLocalRowsWithoutTouchingThem() = runBlocking {
        api.onLocationItems = { throw IOException("нет сети") }

        val result = repo.getItemsByLocationId("C1").map { it.id }.toSet()

        assertEquals(setOf("u1", "s1"), result)
        assertEquals(3, rows.size)
    }
}
