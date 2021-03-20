package com.hs.batch.component

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class SimpleJobTasklet : Tasklet {

    @Value("#{jobParameters[requestDate]}")
    private lateinit var requestDate: String

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        logger.info(">>>>> tasklet 생성")
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        logger.info(">>>>> This is step1")
        logger.info(">>>>> requestDate = {}", requestDate)
        return RepeatStatus.FINISHED
    }
}
