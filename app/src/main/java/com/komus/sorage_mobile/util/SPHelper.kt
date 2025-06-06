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

    fun saveTypeBrief(brief: String) {
        sharedPreferences.edit().putString("briefType", brief).apply()
    }

    fun getTypeBrief(): String {
        return sharedPreferences.getString("briefType", "") ?: ""
    }

    fun saveFullQnt(fullQnt: Int) {
        sharedPreferences.edit().putInt("fullQnt", fullQnt).apply()
    }

    fun saveProductQnt(fullQnt: Int) {
        sharedPreferences.edit().putInt("productQnt", fullQnt).apply()
    }
    fun getProductQnt(): Int {
        return sharedPreferences.getInt("productQnt", 0)
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

    // Методы для работы с размещением товара

    fun saveStorageLocation(location: String) {
        sharedPreferences.edit().putString("storageLocation", location).apply()
    }

    fun getStorageLocation(): String {
        return sharedPreferences.getString("storageLocation", "") ?: ""
    }

    // Методы для работы с буфером

    fun saveBufferLocation(location: String) {
        sharedPreferences.edit().putString("bufferLocation", location).apply()
    }

    fun getBufferLocation(): String {
        return sharedPreferences.getString("bufferLocation", "") ?: ""
    }

    fun saveUserName(userName: String) {
        sharedPreferences.edit().putString("userName", userName).apply()
    }

    fun getUserName(): String {
        return sharedPreferences.getString("userName", "user") ?: "user"
    }

    fun saveWrShk(wrShk: String) {
        sharedPreferences.edit().putString("wrShk", wrShk).apply()
    }

    fun getWrShk(): String {
        return sharedPreferences.getString("wrShk", "") ?: ""
    }

    // Методы для работы с именем продукта
    fun saveProductName(productName: String) {
        sharedPreferences.edit().putString("productName", productName).apply()
    }

    fun getProductName(): String {
        return sharedPreferences.getString("productName", "") ?: ""
    }

    // Методы для работы со штрихкодом
    fun saveShk(shk: String) {
        sharedPreferences.edit().putString("shk", shk).apply()
    }

    fun getShk(): String {
        return sharedPreferences.getString("shk", "") ?: ""
    }

    // Методы для работы с артикулом
    fun saveArticle(article: String) {
        sharedPreferences.edit().putString("article", article).apply()
    }

    fun getArticle(): String {
        return sharedPreferences.getString("article", "") ?: ""
    }

    // Методы для работы с ID склада
    fun saveSkladId(skladId: String) {
        sharedPreferences.edit().putString("skladId", skladId).apply()
    }

    fun getSkladId(): String {
        return sharedPreferences.getString("skladId", "85") ?: "85"
    }

    // Методы для работы с причиной некондиции
    fun saveReason(reason: String) {
        sharedPreferences.edit().putString("reason", reason).apply()
    }

    fun getReason(): String {
        return sharedPreferences.getString("reason", "") ?: ""
    }

    fun clearReason() {
        sharedPreferences.edit().remove("reason").apply()
    }

}