package com.hs.batch.job

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DeciderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun deciderJob(): Job {
        return jobBuilderFactory.get("deciderJob")
            .start(startStep())
            .next(decider()) // 홀수, 짝수 구분
            .from(decider()) // decider의 상태가
                .on("ODD") // ODD이면?
                .to(oddStep()) // oddStep으로 간다.
            .from(decider()) // decider의 상태가
                .on("EVEN") // EVEN이면?
                .to(evenStep()) // evenStep으로 간다.
            .end() // builder 종료
            .build()
    }

    @Bean
    fun startStep(): Step {
        return stepBuilderFactory.get("startStep")
            .tasklet { _, _ ->
                logger.info(">>>>> Start!")
                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun evenStep(): Step {
        return stepBuilderFactory.get("evenStep")
            .tasklet { _, _ ->
                logger.info(">>>>> 짝수입니다!")
                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun oddStep(): Step {
        return stepBuilderFactory.get("oddStep")
            .tasklet { _, _ ->
                logger.info(">>>>> 홀수입니다!")
                RepeatStatus.FINISHED
            }
            .build()
    }

    /*
    * JobExecutionDecider
    * - Step들의 Flow 속에서 분기만 담당하는 타입이다.
    *
    * OddDecider 클래스
    * - JobExecutionDecider의 구현체
    * - 분기로직은 OddDecider 클래스가 담당하고 있다.
    * - Step과는 명확히 역할과 책임이 분리된채 진행할 수 있게 되었다.
    * */
    @Bean
    fun decider(): JobExecutionDecider {
        return OddDecider()
    }
}
