package com.hs.batch.job

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StepNextJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun stepNextJob(): Job {
        return jobBuilderFactory.get("stepNextJob")
            .start(step1())
            .next(step2())
            .next(step3())
            .build()
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
            .tasklet { _, _ ->
                logger.info(">>>>> This is step1")
                RepeatStatus.FINISHED
            }.build()
    }

    @Bean
    fun step2(): Step {
        return stepBuilderFactory.get("step2")
            .tasklet { _, _ ->
                logger.info(">>>>> This is step2")
                RepeatStatus.FINISHED
            }.build()
    }

    @Bean
    fun step3(): Step {
        return stepBuilderFactory.get("step3")
            .tasklet { _, _ ->
                logger.info(">>>>> This is step3")
                RepeatStatus.FINISHED
            }.build()
    }
}
