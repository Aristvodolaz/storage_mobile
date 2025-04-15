package com.komus.sorage_mobile.domain.model

enum class NonConformityReason(val displayName: String) {
    EXPIRED("Истек срок годности"),
    DAMAGED("Повреждена упаковка"),
    DEFECTIVE("Бракованный товар"),
    WRONG_STORAGE("Неправильное хранение"),
    OTHER("Другая причина");

    companion object {
        fun fromDisplayName(displayName: String): NonConformityReason? {
            return values().find { it.displayName == displayName }
        }
    }
} 