package com.komus.sorage_mobile.data.api

import com.komus.sorage_mobile.data.response.AuthResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthApi {
    @GET("/auth")
    suspend fun getEmployeeDetails(
        @Query("id") id: String
    ): AuthResponse

}
