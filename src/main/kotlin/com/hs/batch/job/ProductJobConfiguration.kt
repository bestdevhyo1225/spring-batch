package com.hs.batch.job

import com.hs.batch.entity.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory

@Configuration
class ProductJobConfiguration(
    @Value("\${chunk-size}")
    private val chunkSize: Int,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun productJob(): Job {
        return jobBuilderFactory.get("productJob")
            .start(productStep())
            .build()
    }

    @Bean
    fun productStep(): Step {
        return stepBuilderFactory.get("productStep")
            .chunk<Product, Product>(chunkSize)
            .reader(productReader())
            .processor(productProcessor())
            .writer(productWriter())
            .build()
    }

    @Bean
    fun productReader(): JpaPagingItemReader<Product> {
        logger.info("------- productReader() start -------")
        return JpaPagingItemReaderBuilder<Product>()
            .name("productReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT p FROM Product p")
            .build()
    }

    @Bean
    fun productProcessor(): ItemProcessor<Product, Product> {
        logger.info("------- productProcessor() start -------")
        return ItemProcessor<Product, Product> { product ->
            Product.create(
                name = product.name + "_구매",
                amounts = product.amounts * -1,
                createDate = product.createDate,
                productImages = product.productImages
            )
        }
    }

    @Bean
    fun productWriter(): JpaItemWriter<Product> {
        logger.info("------- productWriter() start -------")
        val jpaItemWriter: JpaItemWriter<Product> = JpaItemWriter()
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory)
        return jpaItemWriter
    }
}
