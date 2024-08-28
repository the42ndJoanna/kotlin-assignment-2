import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import model.InventoriesService
import model.Inventory
import model.Product
import model.ProductsService
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
}