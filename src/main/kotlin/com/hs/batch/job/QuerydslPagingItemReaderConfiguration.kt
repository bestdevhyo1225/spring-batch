package com.hs.batch.job

import com.hs.batch.entity.Pay
import com.hs.batch.entity.Pay2
import com.hs.batch.entity.QPay
import com.hs.batch.reader.QuerydslPagingItemReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManagerFactory

@Configuration
class QuerydslPagingItemReaderConfiguration(
    @Value("\${chunk-size}")
    private val chunkSize: Int,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private const val JOB_NAME = "QuerydslPagingItemReader"
        private const val BEAN_PREFIX = JOB_NAME + "_"
    }

    @Bean(value = [JOB_NAME])
    fun job(): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .preventRestart() // 재실행을 막아주는 start 라고 보면 되나?
            .start(step())
            .build()
    }

    @Bean(value = [BEAN_PREFIX + "step"])
    fun step(): Step {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
            .chunk<Pay, Pay2>(chunkSize)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build()
    }

    @Bean(value = [BEAN_PREFIX + "reader"])
    fun reader(): QuerydslPagingItemReader<Pay> {
        return QuerydslPagingItemReader(
            entityManagerFactory = entityManagerFactory,
            queryFunction = { queryFactory -> queryFactory.selectFrom(QPay.pay) },
            pageSize = chunkSize
        )
    }

    private fun processor(): ItemProcessor<Pay, Pay2> {
        return ItemProcessor { pay ->
            Pay2(
                amount = pay.amount,
                txName = pay.txName,
                txDateTime = pay.txDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        }
    }

    @Bean(value = [BEAN_PREFIX + "writer"])
    fun writer(): JpaItemWriter<Pay2> {
        return JpaItemWriterBuilder<Pay2>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
}
