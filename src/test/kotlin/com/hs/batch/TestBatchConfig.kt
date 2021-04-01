package com.hs.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.context.annotation.Bean

@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
class TestBatchConfig
