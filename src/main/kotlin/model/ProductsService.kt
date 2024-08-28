package model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductsService {
    private val BASE_URL = "http://localhost:3000"

    fun getProductsService(): ProductsApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductsApi::class.java)
    }
}