package com.komus.sorage_mobile.domain.repository

import com.komus.sorage_mobile.data.api.AuthApi
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.response.AuthResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthApi
) {
    suspend fun getEmployeeById(id: String): AuthResponse {
        return authService.getEmployeeDetails(id)
    }
}
