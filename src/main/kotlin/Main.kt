import kotlinx.coroutines.runBlocking
import model.InventoriesService
import model.ProductsService

fun main(args: Array<String>) {
    val productsService = ProductsService()
    val inventoryService = InventoriesService()
    val productManager = ProductManager(productsService, inventoryService)

    runBlocking {
        val products = productManager.getProductsWithAdjustedPrices()

        products.forEach { product ->
            println("$product")
        }
    }

}