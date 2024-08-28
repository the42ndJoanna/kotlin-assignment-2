package model

import retrofit2.Response
import retrofit2.http.GET

interface InventoriesApi {
    @GET("/inventories")
    suspend fun getInventories(): Response<List<Inventory>>
}