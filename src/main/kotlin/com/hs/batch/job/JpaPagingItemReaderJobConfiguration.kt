package com.hs.batch.job

import com.hs.batch.entity.Pay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManagerFactory

@Configuration
class JpaPagingItemReaderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val chunkSize: Int = 10

    @Bean
    fun jpaPagingItemReaderJob(): Job {
        return jobBuilderFactory.get("jpaPagingItemReaderJob")
            .start(jpaPagingItemReaderStep())
            .build()
    }

    @Bean
    fun jpaPagingItemReaderStep(): Step {
        return stepBuilderFactory.get("jpaPagingItemReaderStep")
            .chunk<Pay, Pay>(chunkSize) // 첫번째 Pay는 Reader에서 반환할 타입, 두번째 Pay는 Writer에 파라미터로 넘어올 타입
            .reader(jpaPagingItemReader())
            .writer(jpaPagingItemWriter())
            .build()
    }

    @Bean
    fun jpaPagingItemReader(): JpaPagingItemReader<Pay> {
        return JpaPagingItemReaderBuilder<Pay>()
            .pageSize(chunkSize)
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT p FROM Pay p WHERE amount >= 2000")
            .name("jpaPagingItemReader")
            .build()
    }

    private fun jpaPagingItemWriter(): ItemWriter<Pay> {

        return ItemWriter<Pay> { list ->
            for (pay in list) {
                logger.info("Pay Id = {}", pay.id)
                logger.info(
                    "Pay txDateTime = {}",
                    pay.txDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                logger.info("Current Pay = {}", pay)
            }
        }
    }
}
