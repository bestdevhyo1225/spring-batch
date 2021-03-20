package com.hs.batch.job

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import java.util.*

class OddDecider : JobExecutionDecider {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /*
    * JobExecutionDecider 인터페이스를 구현한 OddDecider 클래스
    *
    * Step으로 처리하는 것이 아니기 때문에 ExitStatus가 아닌 FlowExecutionStatus 클래스를 생성해서 관리합니다.
    * */

    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        val rand = Random()

        val randomNumber = rand.nextInt(50) + 1

        logger.info("랜덤 숫자 : {}", randomNumber)

        return if (randomNumber % 2 == 0) {
            FlowExecutionStatus("EVEN")
        } else {
            FlowExecutionStatus("ODD")
        }
    }
}
