import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import model.*
import retrofit2.Response

class ProductManager(
    private val productsService: ProductsService,
    private val inventoriesService: InventoriesService
) {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Caught exception: ${exception.message}")
    }
    suspend fun getProductsWithAdjustedPrices(): List<Product> = coroutineScope {
        val productResponseDeferred = async(exceptionHandler) { productsService.getProductsService().getProducts() }
        val inventoryResponseDeferred = async(exceptionHandler) { inventoriesService.getInventoriesService().getInventories() }
        val getProductRes = productResponseDeferred.await()
        val getInventoryRes = inventoryResponseDeferred.await()

        if (getProductRes.isSuccessful && getInventoryRes.isSuccessful) {
            val products = getProductRes.body() ?: emptyList()
            val inventories = getInventoryRes.body() ?: emptyList()
            val adjustedProductsDeferred = products.map { product ->
                async {
                    val totalStock = inventories
                        .filter { it.SKU == product.SKU }
                        .sumOf { it.quantity }

                    val adjustedPrice = if (product.type == "HIGH_DEMAND") {
                        when {
                            totalStock > 100 -> product.price
                            totalStock in 31..100 -> product.price * 1.2
                            totalStock <= 30 -> product.price * 1.5
                            else -> product.price
                        }
                    } else {
                        product.price
                    }

                    product.copy(price = adjustedPrice)
                }
            }
            return@coroutineScope adjustedProductsDeferred.awaitAll()
        } else {
            val errorMessage = buildErrorMessage(getProductRes, getInventoryRes)
            throw Exception(errorMessage)
        }
    }

    private fun buildErrorMessage(
        productResponse: Response<List<Product>>,
        inventoryResponse: Response<List<Inventory>>
    ): String {
        return "Failed to fetch products or inventories: " +
                "Product Response: ${productResponse.message()} " +
                "Inventory Response: ${inventoryResponse.message()}"
    }
}