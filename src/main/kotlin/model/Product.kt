package model

data class Product(
    val id: String,
    val SKU: String,
    val name: String,
    val price: Double,
    val type: ProductTypeEnum,
    val image: String
)