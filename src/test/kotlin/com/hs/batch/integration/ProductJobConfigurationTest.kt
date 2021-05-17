package com.hs.batch.integration

import com.hs.batch.TestBatchConfig
import com.hs.batch.entity.Product
import com.hs.batch.entity.ProductImage
import com.hs.batch.job.ProductJobConfiguration
import com.hs.batch.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest(classes = [TestBatchConfig::class, ProductJobConfiguration::class])
@SpringBatchTest
class ProductJobConfigurationTest {

    @Autowired(required = false)
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var productRepository: ProductRepository

    @BeforeEach
    fun init() {
        println("------- BeforeEach start -------")
        val productImages1: List<ProductImage> = listOf(ProductImage(url = "url-1"), ProductImage(url = "url-2"))
        val productImages2: List<ProductImage> = listOf(ProductImage(url = "url-3"), ProductImage(url = "url-4"))

        val product1 =
            Product.create(name = "상품1", amounts = 10_000, createDate = LocalDate.now(), productImages = productImages1)
        val product2 =
            Product.create(name = "상품2", amounts = 20_000, createDate = LocalDate.now(), productImages = productImages2)

        productRepository.save(product1)
        productRepository.save(product2)
    }

    @Test
    fun `Product를 가공해서 Product에 이관한다`() {
        // given

        // when
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob()

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val products: List<Product> = productRepository.findAll()

        for (product in products) {
            println("------------- [1] After -------------")
            println("product.id = " + product.id)
            println("product.name = " + product.name)
            println("product.amounts = " + product.amounts)
        }

        val products2: List<Product> = productRepository.findProductsWithFetchJoin()

        for (product in products2) {
            println("------------- [2] After -------------")
            println("product.id = " + product.id)
            println("product.name = " + product.name)
            println("product.amounts = " + product.amounts)
        }
    }
}
