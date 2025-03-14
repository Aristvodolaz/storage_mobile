package com.komus.sorage_mobile.util

import android.content.SharedPreferences

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveProductId(productId: String) {
        sharedPreferences.edit().putString("productId", productId).apply()
    }

    fun getProductId(): String {
        return sharedPreferences.getString("productId", "") ?: ""
    }

    fun saveBrief(brief: String) {
        sharedPreferences.edit().putString("brief", brief).apply()
    }

    fun getBrief(): String {
        return sharedPreferences.getString("brief", "") ?: ""
    }

    fun saveFullQnt(fullQnt: Int) {
        sharedPreferences.edit().putInt("fullQnt", fullQnt).apply()
    }

    fun getFullQnt(): Int {
        return sharedPreferences.getInt("fullQnt", 0)
    }

    fun saveSrokGodnosti(srokGodnosti: String) {
        sharedPreferences.edit().putString("srokGodnosti", srokGodnosti).apply()
    }

    fun getSrokGodnosti(): String {
        return sharedPreferences.getString("srokGodnosti", "") ?: ""
    }

    fun saveCondition(condition: String) {
        sharedPreferences.edit().putString("condition", condition).apply()
    }

    fun getCondition(): String {
        return sharedPreferences.getString("condition", "") ?: ""
    }

    // Методы для работы с перемещением товара

    fun saveSourceLocation(location: String) {
        sharedPreferences.edit().putString("sourceLocation", location).apply()
    }

    fun getSourceLocation(): String {
        return sharedPreferences.getString("sourceLocation", "") ?: ""
    }

    fun saveTargetLocation(location: String) {
        sharedPreferences.edit().putString("targetLocation", location).apply()
    }

    fun getTargetLocation(): String {
        return sharedPreferences.getString("targetLocation", "") ?: ""
    }

    fun saveQuantity(quantity: Int) {
        sharedPreferences.edit().putInt("quantity", quantity).apply()
    }

    fun getQuantity(): Int {
        return sharedPreferences.getInt("quantity", 0)
    }

    fun clearMovementData() {
        sharedPreferences.edit()
            .remove("productId")
            .remove("sourceLocation")
            .remove("targetLocation")
            .remove("quantity")
            .apply()
    }
}