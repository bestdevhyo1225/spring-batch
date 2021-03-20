package com.hs.batch.job

import com.hs.batch.component.SimpleJobTasklet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SimpleJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val simpleJobTasklet: SimpleJobTasklet
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /*
    * Job
    * - 하나의 Batch 작업 단위를 의마한다.
    * - Job 안에는 여러 Step이 존재한다.
    * */
    @Bean
    fun simpleJob(): Job {
        return jobBuilderFactory.get("simpleJob")
            .start(simpleStep1())
            .next(simpleStep2(null))
            .build()
    }

    fun simpleStep1(): Step {
        return stepBuilderFactory.get("simpleStep1")
            .tasklet(simpleJobTasklet)
            .build()
    }

    /*
    * Step
    * - Tasklet 혹은 Reader & Processor & Writer 묶음이 존재한다. (한 묶음이 같은 레벨)
    * - Reader & Processor가 끝나고 Tasklet으로 마무리 짓는 수행을 할 수 없다.
    *
    * tasklet { contribution, chunkContext -> }
    * - Step 안에서 수행될 기능들을 명시한다.
    * - Tasklet은 Step 안에서 단일로 수행될 커스텀한 기능들을 선언할 때 사용한다.
    * */
    @Bean
    @JobScope
    fun simpleStep2(@Value("#{jobParameters[requestDate]}") requestDate: String?): Step {
        return stepBuilderFactory.get("simpleStep2")
            .tasklet { _, _ ->
                logger.info(">>>>> This is step2")
                logger.info(">>>>> requestDate = {}", requestDate)
                RepeatStatus.FINISHED
            }
            .build()
    }
}
