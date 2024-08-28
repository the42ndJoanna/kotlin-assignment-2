package model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InventoriesService {
    private val BASE_URL = "http://localhost:3000"

    fun getInventoriesService(): InventoriesApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InventoriesApi::class.java)
    }
}