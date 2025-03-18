package com.komus.sorage_mobile.data.api

import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.request.PickFromLocationRequest
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.data.response.AuthResponse
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.InventoryItemResponse
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import com.komus.sorage_mobile.data.response.LocationProductsResponse
import com.komus.sorage_mobile.data.response.LocationInventoryResponse
import com.komus.sorage_mobile.data.response.ProductInfoResponse
import com.komus.sorage_mobile.data.response.ProductSearchResponse
import com.komus.sorage_mobile.data.response.SearchResponse
import com.komus.sorage_mobile.data.response.UnitResponse
import com.komus.sorage_mobile.data.response.UpdateInventoryRequest
import com.komus.sorage_mobile.data.response.SyncInventoryRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT

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
        @Body request: MoveProductRequest
    ): BaseResponse
    
    @GET("/api/storage/location-items/{locationId}")
    suspend fun getLocationItems(
        @Path("locationId") locationId: String,
        @Query("id_sklad") id_sklad: String,
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
    
    @POST("/api/storage/{productId}/buffer")
    suspend fun placeProductToBuffer(
        @Path("productId") productId: String,
        @Body request: PlaceProductRequest
    ): BaseResponse
    
    @POST("/api/storage/pick-from-location")
    suspend fun pickFromLocation(
        @Body request: PickFromLocationRequest
    ): BaseResponse
    
    @POST("/api/storage/pick-from-location-by-sklad-id")
    suspend fun pickFromLocationBySkladId(
        @Body request: PickFromLocationRequest
    ): BaseResponse

    @GET("/api/storage/inventory/location/{locationId}")
    suspend fun getLocationProducts(
        @Path("locationId") locationId: String,
        @Query("sklad_id") skladId: Int? = null
    ): LocationProductsResponse
    
    @GET("/api/storage/article-info")
    suspend fun getProductInfo(
        @Query("article") article: String? = null,
        @Query("shk") shk: String? = null,
        @Query("id_sklad") skladId: Int? = null
    ): ProductInfoResponse
    
    @GET("storage/location-items/{locationId}")
    suspend fun getLocationItems(
        @Path("locationId") locationId: String,
        @Query("wrShk") wrShk: String,
        @Query("id_sklad") id_sklad: Int
    ): LocationItemsResponse

    @GET("/api/storage/inventory/article/{article}")
    suspend fun getInventoryItemByArticle(
        @Path("article") article: String,
        @Query("sklad_id") skladId: String? = null
    ): InventoryItemResponse

    @GET("/api/storage/inventory/barcode/{barcode}")
    suspend fun getInventoryItemByBarcode(
        @Path("barcode") barcode: String,
        @Query("sklad_id") skladId: String? = null
    ): InventoryItemResponse

    @GET("/api/storage/inventory/location/{locationId}")
    suspend fun getInventoryItemsByLocation(
        @Path("locationId") locationId: String,
        @Query("sklad_id") skladId: String? = null
    ): LocationInventoryResponse

    @PUT("/api/storage/inventory/update")
    suspend fun updateInventoryItem(
        @Body request: UpdateInventoryRequest
    ): BaseResponse

    @POST("/api/storage/inventory/sync")
    suspend fun syncInventoryResults(
        @Body request: SyncInventoryRequest
    ): BaseResponse

}