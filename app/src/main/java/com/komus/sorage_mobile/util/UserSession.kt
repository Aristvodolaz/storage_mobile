package com.komus.sorage_mobile.util

/** Данные текущего пользователя и склада, нужные репозиториям. Реализуется SPHelper; в тестах подменяется. */
interface UserSession {
    fun getUserName(): String
    fun getSkladId(): String
}
