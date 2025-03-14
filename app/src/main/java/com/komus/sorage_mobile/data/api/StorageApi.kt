package com.komus.sorage_mobile.data.api

import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.response.AuthResponse
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import com.komus.sorage_mobile.data.response.ProductSearchResponse
import com.komus.sorage_mobile.data.response.SearchResponse
import com.komus.sorage_mobile.data.response.UnitResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StorageApi {

    @GET("/auth")
    suspend fun getEmployeeDetails(
        @Query("id") id: String
    ): AuthResponse

    @GET("/api/storage/search")
    suspend fun searchItem(
        @Query("shk") shk: String?,
        @Query("article") article: String?
    ): SearchResponse

    @GET("/api/storage/{productId}/units")
    suspend fun getUnits(
        @Path("productId") productId: String
    ): UnitResponse
    
    @POST("/api/storage/{productId}/buffer/move")
    suspend fun moveProduct(
        @Path("productId") productId: String,
        @Body requestBody: Map<String, Any>
    ): BaseResponse
    
    @GET("/api/storage/location/{locationId}/items")
    suspend fun getLocationItems(
        @Path("locationId") locationId: String
    ): LocationItemsResponse
    
    @POST("/api/storage/{productId}/location/pick")
    suspend fun pickProduct(
        @Path("productId") productId: String,
        @Body request: PickRequest
    ): BaseResponse
    
    @GET("/api/storage/item/details")
    suspend fun getProductDetails(
        @Query("shk") shk: String?,
        @Query("article") article: String?
    ): ProductSearchResponse
}