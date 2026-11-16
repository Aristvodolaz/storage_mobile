package com.komus.sorage_mobile.domain.repository

import com.komus.sorage_mobile.data.api.AuthApi
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.response.AuthResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthApi,
    private val cache: ResponseCache
) {
    /** Сотрудник запоминается, чтобы можно было войти без сети. */
    suspend fun getEmployeeById(id: String): AuthResponse {
        return cache.fetch(
            key = "employee:$id",
            clazz = AuthResponse::class.java,
            isCacheable = { it.success }
        ) {
            authService.getEmployeeDetails(id)
        }
    }
}
