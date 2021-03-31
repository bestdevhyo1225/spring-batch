package com.hs.batch.job

import com.hs.batch.entity.Pay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.BeanPropertyRowMapper
import javax.sql.DataSource

@Configuration
class JdbcBatchItemWriterJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val dataSource: DataSource
) {
    private val chunkSize: Int = 10

    @Bean
    fun jdbcBatchItemWriterJob(): Job {
        return jobBuilderFactory.get("jdbcBatchItemWriterJob")
            .start(jdbcBatchItemWriterStep())
            .build()
    }

    @Bean
    fun jdbcBatchItemWriterStep(): Step {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
            .chunk<Pay, Pay>(chunkSize)
            .reader(jdbcItemReaderForBatchItemWriter())
            .writer(jdbcBatchItemWriter())
            .build()
    }

    @Bean
    fun jdbcItemReaderForBatchItemWriter(): JdbcCursorItemReader<Pay> {
        return JdbcCursorItemReaderBuilder<Pay>()
            .fetchSize(chunkSize)
            .dataSource(dataSource)
            .rowMapper(BeanPropertyRowMapper(Pay::class.java))
            .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
            .name("jdbcItemReaderForBatchItemWriter")
            .build()
    }

    @Bean
    fun jdbcBatchItemWriter(): JdbcBatchItemWriter<Pay> {
        val jdbcBatchItemWriter = JdbcBatchItemWriterBuilder<Pay>()
            .dataSource(dataSource)
            .sql("insert into pay2(amount, tx_name, tx_date_time) values (:amount, :txName, :txDateTime)")
            .beanMapped()
            .build()

        jdbcBatchItemWriter.afterPropertiesSet()

        return jdbcBatchItemWriter
    }
}
