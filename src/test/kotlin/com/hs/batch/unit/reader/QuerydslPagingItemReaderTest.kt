package com.hs.batch.unit.reader

import com.hs.batch.TestBatchConfig
import com.hs.batch.entity.Pay
import com.hs.batch.entity.QPay
import com.hs.batch.entity.QPay.pay
import com.hs.batch.reader.QuerydslPagingItemReader
import com.hs.batch.repository.PayRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManagerFactory

@SpringBootTest(classes = [TestBatchConfig::class])
class QuerydslPagingItemReaderTest {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var payRepository: PayRepository

    @AfterEach
    fun tearDown() {
        payRepository.deleteAllInBatch()
    }

    @Test
    fun `reader가 정상적으로 값을 반환한다`() {
        // given
        val firstPay = Pay(amount = 1000L, txName = "이름1", txDateTime = LocalDateTime.now().format(dateTimeFormatter))
        val secondPay = Pay(amount = 2000L, txName = "이름2", txDateTime = LocalDateTime.now().format(dateTimeFormatter))

        payRepository.save(firstPay)
        payRepository.save(secondPay)

        val pageSize = 1

        // when
        val reader: QuerydslPagingItemReader<Pay> = QuerydslPagingItemReader(
            entityManagerFactory = entityManagerFactory,
            queryFunction = { queryFactory -> queryFactory.selectFrom(pay) },
            pageSize = pageSize
        )

        reader.open(ExecutionContext())

        // then
        val read1: Pay? = reader.read()
        val read2: Pay? = reader.read()
        val read3: Pay? = reader.read()

        assertThat(read1?.amount).isEqualTo(1000)
        assertThat(read2?.amount).isEqualTo(2000)
        assertThat(read3).isNull() // ItemReader에서는 더이상 읽을 데이터가 없을 경우 read()에서 null을 반환한다.
    }
}
