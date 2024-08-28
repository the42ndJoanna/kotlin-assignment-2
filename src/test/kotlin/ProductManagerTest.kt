import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class ProductManagerTest {

    @MockK
    private lateinit var productsService: ProductsService

    @MockK
    private lateinit var inventoriesService: InventoriesService

    private lateinit var productManager: ProductManager

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        productManager = ProductManager(productsService, inventoriesService)
    }

    @Test
    fun `getProductsWithAdjustedPrices should throw an exception when API call fails`() = runBlocking {
        // GIVEN
        val errorResponse: Response<List<Product>> = Response.error(500, mockk(relaxed = true))
        val inventoryErrorResponse: Response<List<Inventory>> = Response.error(500, mockk(relaxed = true))
        coEvery { productsService.getProductsService().getProducts() } returns errorResponse
        coEvery { inventoriesService.getInventoriesService().getInventories() } returns inventoryErrorResponse

       // WHEN
        val exception = assertThrows<Exception> {
                productManager.getProductsWithAdjustedPrices()
        }

        // THEN
        assertTrue(exception.message!!.contains("Failed to fetch products or inventories"))
        coVerify(exactly = 1) { productsService.getProductsService().getProducts() }
        coVerify(exactly = 1) { inventoriesService.getInventoriesService().getInventories() }
    }

    @Test
    fun `getProductsWithAdjustedPrices should return products with original price if type is NORMAL`() = runBlocking {
        // GIVEN
        val products = listOf(
            Product(id = "1", SKU = "ABC123", name = "Electronic Watch", price = 299.99, type = ProductTypeEnum.NORMAL, image = "image1.jpg"),
        )
        val inventories = listOf(
            Inventory(id = "1", SKU = "ABC123", zone = "CN_NORTH", quantity = 50)
        )
        val productResponse: Response<List<Product>> = Response.success(products)
        val inventoryResponse: Response<List<Inventory>> = Response.success(inventories)
        coEvery { productsService.getProductsService().getProducts() } returns productResponse
        coEvery { inventoriesService.getInventoriesService().getInventories() } returns inventoryResponse

        // WHEN
        val result = productManager.getProductsWithAdjustedPrices()

        // THEN
        val expectedProducts = listOf(
            Product(id = "1", SKU = "ABC123", name = "Electronic Watch", price = 299.99, type = ProductTypeEnum.NORMAL, image = "image1.jpg")
        )
        assertEquals(expectedProducts, result)
        coVerify(exactly = 1) { productsService.getProductsService().getProducts() }
        coVerify(exactly = 1) { inventoriesService.getInventoriesService().getInventories() }
    }

    @Test
    fun `getProductsWithAdjustedPrices should return products with adjusted prices if type is HIGH_DEMAND`() = runBlocking {
        // GIVEM
        val products = listOf(
            Product(id = "1", SKU = "ABC123", name = "Electronic Watch", price = 100.00, type = ProductTypeEnum.HIGH_DEMAND, image = "image1.jpg"),
            Product(id = "2", SKU = "DEF456", name = "Sports Shoes", price = 100.00, type = ProductTypeEnum.HIGH_DEMAND, image = "image2.jpg"),
            Product(id = "3", SKU = "GHI789", name = "Bluetooth Headphones", price = 100.00, type = ProductTypeEnum.HIGH_DEMAND, image = "image3.jpg"),
        )
        val inventories = listOf(
            Inventory(id = "1", SKU = "ABC123", zone = "CN_NORTH", quantity = 50),
            Inventory(id = "2", SKU = "ABC123", zone = "CN_EAST", quantity = 100),
            Inventory(id = "3", SKU = "DEF456", zone = "US_WEST", quantity = 100),
            Inventory(id = "4", SKU = "GHI789", zone = "EU_CENTRAL", quantity = 10),
        )
        val productResponse: Response<List<Product>> = Response.success(products)
        val inventoryResponse: Response<List<Inventory>> = Response.success(inventories)
        coEvery { productsService.getProductsService().getProducts() } returns productResponse
        coEvery { inventoriesService.getInventoriesService().getInventories() } returns inventoryResponse

        // WHEN
        val result = productManager.getProductsWithAdjustedPrices()

        // THEN
        val expectedProducts = listOf(
            Product(id = "1", SKU = "ABC123", name = "Electronic Watch", price = 100.00, type = ProductTypeEnum.HIGH_DEMAND, image = "image1.jpg"),
            Product(id = "2", SKU = "DEF456", name = "Sports Shoes", price = 100.00 * 1.2, type = ProductTypeEnum.HIGH_DEMAND, image = "image2.jpg"),
            Product(id = "3", SKU = "GHI789", name = "Bluetooth Headphones", price = 100.00 * 1.5, type = ProductTypeEnum.HIGH_DEMAND, image = "image3.jpg")
        )
        assertEquals(expectedProducts, result)
        coVerify(exactly = 1) { productsService.getProductsService().getProducts() }
        coVerify(exactly = 1) { inventoriesService.getInventoriesService().getInventories() }
    }
}