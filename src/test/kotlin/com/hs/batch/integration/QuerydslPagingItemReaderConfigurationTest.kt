package com.hs.batch.integration

import com.hs.batch.TestBatchConfig
import com.hs.batch.entity.Pay
import com.hs.batch.entity.Pay2
import com.hs.batch.job.QuerydslPagingItemReaderConfiguration
import com.hs.batch.repository.Pay2Repository
import com.hs.batch.repository.PayRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(classes = [TestBatchConfig::class, QuerydslPagingItemReaderConfiguration::class])
@SpringBatchTest
class QuerydslPagingItemReaderConfigurationTest {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @Autowired(required = false)
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var payRepository: PayRepository

    @Autowired
    private lateinit var pay2Repository: Pay2Repository

    @AfterEach
    fun afterEach() {
        payRepository.deleteAllInBatch()
        pay2Repository.deleteAllInBatch()
    }

    @Test
    fun `Pay가 Pay2로 이관된다`() {
        // given
        val firstPay = Pay(amount = 1000L, txName = "이름1", txDateTime = LocalDateTime.now().format(dateTimeFormatter))
        val secondPay = Pay(amount = 2000L, txName = "이름2", txDateTime = LocalDateTime.now().format(dateTimeFormatter))

        payRepository.save(firstPay)
        payRepository.save(secondPay)

        // when
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob()

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val findResult: List<Pay2> = pay2Repository.findAll()

        assertThat(findResult.size).isEqualTo(2)
        assertThat(findResult[0].amount).isEqualTo(firstPay.amount)
        assertThat(findResult[0].txName).isEqualTo(firstPay.txName)
        assertThat(findResult[0].txDateTime).isEqualTo(firstPay.txDateTime)
        assertThat(findResult[1].amount).isEqualTo(secondPay.amount)
        assertThat(findResult[1].txName).isEqualTo(secondPay.txName)
        assertThat(findResult[1].txDateTime).isEqualTo(secondPay.txDateTime)
    }
}
