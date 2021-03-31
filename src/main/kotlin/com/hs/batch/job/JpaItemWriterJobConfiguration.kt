package com.hs.batch.job

import com.hs.batch.entity.Pay
import com.hs.batch.entity.Pay2
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManagerFactory

@Configuration
class JpaItemWriterJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {
    private val chunkSize: Int = 10

    @Bean
    fun jpaItemWriterJob(): Job {
        return jobBuilderFactory.get("jpaItemWriterJob")
            .start(jpaItemWriterStep())
            .build()
    }

    @Bean
    fun jpaItemWriterStep(): Step {
        return stepBuilderFactory.get("jpaItemWriterStep")
            .chunk<Pay, Pay2>(chunkSize)
            .reader(jpaItemWriterReader())
            .processor(jpaItemProcessor())
            .writer(jpaItemWriter())
            .build()
    }

    @Bean
    fun jpaItemWriterReader(): JpaPagingItemReader<Pay> {
        return JpaPagingItemReaderBuilder<Pay>()
            .name("jpaItemWriterReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT p FROM Pay p")
            .build()
    }

    @Bean
    fun jpaItemProcessor(): ItemProcessor<Pay, Pay2> {
        return ItemProcessor<Pay, Pay2> { pay ->
            Pay2(
                pay.amount,
                pay.txName,
                pay.txDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        }
    }

    @Bean
    fun jpaItemWriter(): JpaItemWriter<Pay2> {
        val jpaItemWriter: JpaItemWriter<Pay2> = JpaItemWriter()
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory)
        return jpaItemWriter
    }
}
