package com.komus.sorage_mobile.data.api

import com.google.gson.annotations.SerializedName
import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.request.PickFromLocationRequest
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.data.response.ArticleInventoryResponse
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
import com.komus.sorage_mobile.data.model.StorageResponse
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
        @Query("sklad_id") skladId: Int
    ): LocationProductsResponse
    
    @GET("/api/storage/article-info")
    suspend fun getProductInfo(
        @Query("article") article: String? = null,
        @Query("shk") shk: String? = null,
        @Query("id_sklad") skladId: Int? = null
    ): ProductInfoResponse

    
    @GET("/api/storage/location-items/{locationId}")
    suspend fun getLocationItems(
        @Path("locationId") locationId: String,
        @Query("id_sklad") id_sklad: Int
    ): LocationItemsResponse

    @GET("/api/storage/article-info")
    suspend fun getInventoryItemByArticle(
        @Query("article") article: String,
        @Query("id_sklad") skladId: String
    ): ArticleInventoryResponse

    @POST("/api/storage/inventory")
    suspend fun confirmInventoryItem(
        @Body request: InventoryRequest
    ): BaseResponse

    @GET("/api/storage/all")
    suspend fun getAllStorageItems(
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0,
        @Query("id_sklad") warehouseId: Int = 85
    ): StorageResponse

}

data class InventoryRequest(
    @SerializedName("id") val id: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("expirationDate") val expirationDate: String,
    @SerializedName("conditionState") val conditionState: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("executor") val executor: String
)