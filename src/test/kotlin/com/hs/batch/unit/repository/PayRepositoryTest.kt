package com.hs.batch.unit.repository

import com.hs.batch.TestBatchConfig
import com.hs.batch.entity.Pay
import com.hs.batch.repository.PayJdbcRepository
import com.hs.batch.repository.PayRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(classes = [TestBatchConfig::class, PayJdbcRepository::class])
@ActiveProfiles(value = ["mysql"])
class PayRepositoryTest {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @Autowired
    private lateinit var payRepository: PayRepository

    @Autowired
    private lateinit var payJdbcRepository: PayJdbcRepository

    @Test
    fun `payRepository의 saveAll 메소드 수행 시간을 측정한다`() {
        val pays: MutableList<Pay> = mutableListOf()
        for (i in 0 until 1000) {
            pays.add(Pay(amount = 100L, txName = "pay_$i", txDateTime = LocalDateTime.now().format(dateTimeFormatter)))
        }

        val startTime = System.currentTimeMillis()

        payRepository.saveAll(pays)

        val endTime = System.currentTimeMillis()

        println("execution time = " + (endTime - startTime) + "ms")
    }

    @Test
    fun `payJdbcRepository의 saveAll 메소드 수행 시간을 측정한다`() {
        val pays: MutableList<Pay> = mutableListOf()
        for (i in 0 until 1000) {
            pays.add(Pay(amount = 100L, txName = "pay_$i", txDateTime = LocalDateTime.now().format(dateTimeFormatter)))
        }

        val startTime = System.currentTimeMillis()

        payJdbcRepository.saveAll(pays)

        val endTime = System.currentTimeMillis()

        println("execution time = " + (endTime - startTime) + "ms")
    }
}
