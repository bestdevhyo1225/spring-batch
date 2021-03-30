package com.hs.batch.job

import com.hs.batch.entity.Pay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.jdbc.core.BeanPropertyRowMapper


@Configuration
class JdbcCursorItemReaderJobConfiguration(
    private val dataSource: DataSource,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val chunkSize: Int = 10

    @Bean
    fun jdbcCursorItemReaderJob(): Job {
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
            .start(jdbcCursorItemReaderStep())
            .build()
    }

    @Bean
    fun jdbcCursorItemReaderStep(): Step {
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
            .chunk<Pay, Pay>(chunkSize)
            .reader(jdbcCursorItemReader())
            .writer(jdbcCursorItemWriter())
            .build()
    }

    @Bean
    fun jdbcCursorItemReader(): JdbcCursorItemReader<Pay> {
        return JdbcCursorItemReaderBuilder<Pay>()
            .fetchSize(chunkSize)
            .dataSource(dataSource)
            .rowMapper(BeanPropertyRowMapper(Pay::class.java))
            .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
            .name("jdbcCursorItemReader")
            .build()
    }

    private fun jdbcCursorItemWriter(): ItemWriter<Pay> {
        return ItemWriter<Pay> { list ->
            for (pay in list) {
                logger.info("Pay Id = {}", pay.id)
                logger.info("Current Pay = {}", pay)
            }
        }
    }
}
