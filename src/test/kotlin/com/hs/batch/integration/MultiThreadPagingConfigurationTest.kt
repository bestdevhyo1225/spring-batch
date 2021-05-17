package com.hs.batch.integration

import com.hs.batch.TestBatchConfig
import com.hs.batch.entity.Product
import com.hs.batch.entity.ProductBackup
import com.hs.batch.entity.ProductImage
import com.hs.batch.job.MultiThreadPagingConfiguration
import com.hs.batch.repository.ProductBackupRepository
import com.hs.batch.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@SpringBatchTest
@SpringBootTest(classes = [TestBatchConfig::class, MultiThreadPagingConfiguration::class])
@TestPropertySource(properties = ["chunk-size=1", "pool-size=2"])
class MultiThreadPagingConfigurationTest {

    @Autowired(required = false)
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var productBackupRepository: ProductBackupRepository

    @Test
    fun `페이징이 분산처리 된다`() {
        // given
        val createDate = LocalDate.of(2020, 4, 13)
        val products: MutableList<Product> = mutableListOf()

        for (i in 1..10) {
            val name = "상품_$i"
            val amounts = i * 10_000
            val productImages: List<ProductImage> = listOf(ProductImage(url = "image_$i"))
            val product =
                Product.create(name = name, amounts = amounts, createDate = createDate, productImages = productImages)

            products.add(product)
        }

        productRepository.saveAll(products)

        val jobParameters: JobParameters = JobParametersBuilder()
            .addString("createDate", createDate.toString())
            .toJobParameters()

        // when
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val productBackups: List<ProductBackup> = productBackupRepository.findAll()

        assertThat(productBackups).hasSize(10)

        var amounts = 10_000
        for (productBackup in productBackups) {
            assertThat(productBackup.amounts).isEqualTo(amounts)
            amounts += 10_000
        }
    }
}
