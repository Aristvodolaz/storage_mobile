package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.response.ProductInfo
import com.komus.sorage_mobile.data.response.ProductItem
import com.komus.sorage_mobile.data.response.ProductLocation

/**
 * Сопоставление ответа поиска с ProductInfo: количество по всем местам товара.
 * productQnt в ответе поиска не передаётся по местам, поэтому берём количество места.
 */
internal fun ProductItem.toProductInfo(): ProductInfo {
    val locations = units.values.flatMap { unit ->
        unit.locations.map { loc ->
            ProductLocation(
                id = loc.id.toIntOrNull() ?: 0,
                name_wr = loc.wrShk,
                quantity = loc.quantity,
                productQnt = loc.quantity,
                conditionState = loc.conditionState,
                expirationDate = loc.expirationDate
            )
        }
    }
    return ProductInfo(
        id = id.toIntOrNull() ?: 0,
        name = name,
        shk = shk,
        article = article,
        totalQuantity = locations.sumOf { it.quantity },
        items = locations
    )
}
