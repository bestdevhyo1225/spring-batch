package com.hs.batch.job

import com.hs.batch.entity.Teacher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory

@Configuration
class ProcessorConvertJobConfiguration(
    @Value("\${chunk-size}")
    private val chunkSize: Int,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private const val JOB_NAME = "ProcessorConvertBatch"
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
    @JobScope
    fun step(): Step {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
            .chunk<Teacher, String>(chunkSize)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build()
    }

    @Bean(value = [BEAN_PREFIX + "reader"])
    fun reader(): JpaPagingItemReader<Teacher> {
        return JpaPagingItemReaderBuilder<Teacher>()
            .name(BEAN_PREFIX + "reader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT t FROM Teacher t")
            .build()
    }

    @Bean(value = [BEAN_PREFIX + "processor"])
    fun processor(): ItemProcessor<Teacher, String> {
        return ItemProcessor { teacher ->
            teacher.name
        }
    }

    private fun writer(): ItemWriter<String> {
        return ItemWriter { items ->
            for (item in items) {
                logger.info("Teacher Name = {}", item)
            }
        }
    }
}
