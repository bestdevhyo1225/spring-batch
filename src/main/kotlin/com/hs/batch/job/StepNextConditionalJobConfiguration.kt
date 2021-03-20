package com.hs.batch.job

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StepNextConditionalJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * 성공 시나리오 : step1 -> step2 -> step3
     * 실패 시나리오 : step1 -> step3
     */

    /**
     * on()
     * - 캐치할 ExitStatus 지정
     * - '*' 일 경우, 모든 ExitStatus가 지정된다.
     * - 캐치하는 상태값이 BatchStatus가 아닌 ExitStatus라는 점이다.
     * - 분기 처리를 위해 상태값 조정이 필요하다면, ExitStatus를 조정해야한다.
     *
     * to()
     * - 다음으로 이동할 Step 지정
     *
     * from()
     * - 일종의 '이벤트 리스너' 역할
     * - 상태값을 보고 일치하는 상태라면 to()에 포함된 Step을 호출
     * - Step1의 이벤트 캐치가 FAILED로 되어 있는 상태에서 추가적으로 이벤트를 캐치하려면, from()을 써야한다.
     *
     * end()
     * - FlowBuilder를 반환하는 end와 FlowBuilder를 종료하는 end 2개가 있다.
     * - on() 뒤에 있는 end()는 FlowBuilder를 반환하는 end
     * - build() 앞에 있는 end()는 FlowBuilder를 종료하는 end
     * - FlowBuilder를 반환하는 end 사용시, 계속해서 from()을 이어갈 수 있다.
     */

    @Bean
    fun stepNextConditionalJob(): Job {
        return jobBuilderFactory.get("stepNextConditionalJob")
            .start(conditionalJobStep1())
                .on("FAILED") // FAILED일 경우
                .to(conditionalJobStep3()) // Step3으로 이동한다.
                .on("*") // Step3의 결과와 관계 없이
                .end() // Step3으로 이동하면, Flow가 종료된다.
            .from(conditionalJobStep1())
                .on("*") // FAILED 외에 모든 경우
                .to(conditionalJobStep2()) // step2로 이동한다.
                .next(conditionalJobStep3()) // step2가 정상 종료되면 step3으로 이동한다.
                .on("*") // Step3의 결과와 관계 없이
                .end() // Step3으로 이동하면, Flow가 종료된다.
            .end() // Job 종료
            .build()
    }

    @Bean
    fun conditionalJobStep1(): Step {
        return stepBuilderFactory.get("step1")
            .tasklet { contribution, _ ->
                logger.info(">>>>> This is stepNextConditionalJob Step1")

//                contribution.exitStatus = ExitStatus.FAILED

                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun conditionalJobStep2(): Step {
        return stepBuilderFactory.get("conditionalJobStep2")
            .tasklet { _, _ ->
                logger.info(">>>>> This is stepNextConditionalJob Step2")
                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun conditionalJobStep3(): Step {
        return stepBuilderFactory.get("conditionalJobStep3")
            .tasklet { _, _ ->
                logger.info(">>>>> This is stepNextConditionalJob Step3")
                RepeatStatus.FINISHED
            }
            .build()
    }
}
