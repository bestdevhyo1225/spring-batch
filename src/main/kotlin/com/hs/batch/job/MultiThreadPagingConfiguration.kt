package com.hs.batch.job

import com.hs.batch.entity.Product
import com.hs.batch.entity.ProductBackup
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManagerFactory

@Configuration
class MultiThreadPagingConfiguration(
    @Value(value = "\${chunk-size:10}")
    private val chunkSize: Int,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory
) {

    companion object {
        private const val JOB_NAME = "multiThreadPagingBatch"
    }

    private var poolSize: Int = 10
    private var queueCapacity: Int = 100

    @Value(value = "\${pool-size:10}")
    fun setPoolSize(poolSize: Int) {
        this.poolSize = poolSize
    }

    @Value(value = "\${queue-capacity:100}")
    fun setQueueCapacity(queueCapacity: Int) {
        this.queueCapacity = queueCapacity
    }

    @Bean(name = [JOB_NAME + "_taskpool"])
    fun executor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = poolSize
        executor.maxPoolSize = poolSize
        executor.setQueueCapacity(queueCapacity)
        executor.setThreadNamePrefix("Multi-Thread")
        executor.setWaitForTasksToCompleteOnShutdown(true) // 진행 중이던 작업이 완료된 후 Thread를 종료한다. (true)
        executor.initialize()
        return executor
    }

    @Bean(name = [JOB_NAME])
    fun job(): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .start(step())
            .preventRestart()
            .build()
    }

    @Bean(name = [JOB_NAME + "_step"])
    @JobScope
    fun step(): Step {
        return stepBuilderFactory.get(JOB_NAME + "_step")
            .chunk<Product, ProductBackup>(chunkSize)
            .reader(reader(null))
            .processor(processor())
            .writer(writer())
            .taskExecutor(executor())
            .throttleLimit(poolSize)
            .build()
    }

    @Bean(name = [JOB_NAME + "_reader"])
    @StepScope
    fun reader(@Value(value = "#{jobParameters[createDate]}") createDate: String?): JpaPagingItemReader<Product> {
        println("createDate : $createDate")

        val params: Map<String, Any> =
            mapOf("createDate" to LocalDate.parse(createDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        return JpaPagingItemReaderBuilder<Product>()
            .name(JOB_NAME + "_reader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT p FROM Product p WHERE p.createDate =: createDate")
            .parameterValues(params)
            .saveState(false) // 멀티 스레드 환경에서 필수적으로 사용해야하는 옵션
            .build()
    }

    private fun processor(): ItemProcessor<Product, ProductBackup> {
        return ItemProcessor { product ->
            ProductBackup(name = product.name, amounts = product.amounts)
        }
    }

    @Bean(name = [JOB_NAME + "_writer"])
    @StepScope
    fun writer(): JpaItemWriter<ProductBackup> {
        return JpaItemWriterBuilder<ProductBackup>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
}
