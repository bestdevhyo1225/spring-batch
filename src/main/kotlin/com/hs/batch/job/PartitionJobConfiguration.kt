package com.hs.batch.job

import com.hs.batch.entity.Product
import com.hs.batch.entity.ProductBackup
import com.hs.batch.repository.ProductBackupRepository
import com.hs.batch.repository.ProductRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
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
class PartitionJobConfiguration(
    @Value("\${chunk-size}")
    private val chunkSize: Int,
    @Value("\${partition-pool-size:5}")
    private val poolSize: Int,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val productRepository: ProductRepository,
    private val productBackupRepository: ProductBackupRepository,
    private val entityManagerFactory: EntityManagerFactory
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private const val JOB_NAME = "PartitionJobConfiguration"
    }

    @Bean(name = [JOB_NAME + "_taskPool"])
    fun executor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()

        executor.corePoolSize = poolSize
        executor.maxPoolSize = poolSize
        executor.setThreadNamePrefix("partition-thread")
        // Queue ????????? ??? Task ??? ????????? ????????? Shutdown ??????
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()

        return executor
    }

    @Bean(name = [JOB_NAME + "_partitionHandler"])
    fun partitionHanlder(): TaskExecutorPartitionHandler {
        // Local ???????????? Multi Thread??? ??????????????? TaskExecutorPartitionHandler ???????????? ????????????.
        val partitionHandler = TaskExecutorPartitionHandler()

        // Multi Thread??? ???????????? ?????? TaskExecutor??? ????????????.
        partitionHandler.setTaskExecutor(executor())
        // Worker??? ????????? step??? ????????????.
        // Partitioner??? ????????? ??? StepExecutions ???????????? ??????????????? ????????????.
        partitionHandler.step = step1()
        // ????????? ????????? gridSize??? ????????? ????????? poolSize??? gridSize??? ????????????.
        partitionHandler.gridSize = poolSize

        return partitionHandler
    }

    @Bean(name = [JOB_NAME])
    fun job(): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .start(step1Manager())
            .preventRestart()
            .build()
    }

    /*
    * Master Step??? ?????? Step??? Worker??? ???????????? ???????????? ??? ???????????? ????????????.
    * ??? ??? ????????? PartitionHanlder??? ????????????.
    * */
    @Bean(name = [JOB_NAME + "_step1Manager"])
    fun step1Manager(): Step {
        return stepBuilderFactory.get("step1.manager")
            .partitioner("step1", partitioner(null, null)) // step1?????? ????????? Partitioner ???????????? ????????????.
            .step(step1()) // ??????????????? Step??? ????????????. step1??? Partitioner ????????? ?????? ?????? ?????? StepExecutions??? ????????? ????????????.
            .partitionHandler(partitionHanlder())
            .build()
    }

    @Bean(name = [JOB_NAME + "_partitioner"])
    @StepScope
    fun partitioner(
        @Value("#{jobParameters['startDate']}") startDate: String?,
        @Value("#{jobParameters['endDate']}") endDate: String?
    ): ProductIdRangePartitioner {
        val startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return ProductIdRangePartitioner(
            productRepository = productRepository,
            startDate = startLocalDate,
            endDate = endLocalDate
        )
    }

    @Bean(name = [JOB_NAME + "_step"])
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
            .chunk<Product, ProductBackup>(chunkSize)
            .reader(reader(null, null))
            .processor(processor())
            .writer(writer(null, null))
            .build()
    }

    @Bean(name = [JOB_NAME + "_reader"])
    @StepScope
    fun reader(
        @Value("#{stepExecutionContext['minId']}") minId: Long?,
        @Value("#{stepExecutionContext['maxId']}") maxId: Long?,
    ): JpaPagingItemReader<Product> {
        val params: Map<String, Long?> = mapOf("minId" to minId, "maxId" to maxId)

        logger.info("reader minId = {}, maxId = {}", minId, maxId)

        return JpaPagingItemReaderBuilder<Product>()
            .name(JOB_NAME + "_reader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT p FROM Product p WHERE p.id BETWEEN :minId AND :maxId")
            .parameterValues(params)
            .build()
    }

    private fun processor(): ItemProcessor<Product, ProductBackup> {
        return ItemProcessor<Product, ProductBackup> { ProductBackup(name = it.name, amounts = it.amounts) }
    }

    @Bean(name = [JOB_NAME + "_writer"])
    @StepScope
    fun writer(
        @Value("#{stepExecutionContext['minId']}") minId: Long?,
        @Value("#{stepExecutionContext['maxId']}") maxId: Long?,
    ): ItemWriter<ProductBackup> {
        return ItemWriter { productBackupRepository.saveAll(it) }
    }
}
