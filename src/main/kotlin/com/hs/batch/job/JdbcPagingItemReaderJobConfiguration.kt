package com.hs.batch.job

import com.hs.batch.entity.Pay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.BeanPropertyRowMapper
import javax.sql.DataSource

@Configuration
class JdbcPagingItemReaderJobConfiguration(
    private val dataSource: DataSource,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val chunkSize: Int = 10

    @Bean
    fun jdbcPagingItemReaderJob(): Job {
        return jobBuilderFactory.get("jdbcPagingItemReaderJob")
            .start(jdbcPagingItemReaderStep())
            .build()
    }

    @Bean
    fun jdbcPagingItemReaderStep(): Step {
        return stepBuilderFactory.get("jdbcPagingItemReaderStep")
            .chunk<Pay, Pay>(chunkSize)
            .reader(jdbcPagingItemReader())
            .writer(jdbcPagingItemWriter())
            .build()
    }

    @Bean
    fun jdbcPagingItemReader(): JdbcPagingItemReader<Pay> {
        val parameterValues: MutableMap<String, Any> = HashMap()
        parameterValues["amount"] = 200

        return JdbcPagingItemReaderBuilder<Pay>()
            .pageSize(chunkSize)
//            .fetchSize(chunkSize)
            .dataSource(dataSource)
            .rowMapper(BeanPropertyRowMapper(Pay::class.java))
            .queryProvider(createQueryProvider())
            .parameterValues(parameterValues)
            .name("jdbcPagingItemReader")
            .build()
    }

    private fun jdbcPagingItemWriter(): ItemWriter<Pay> {
        return ItemWriter<Pay> { list ->
            for (pay in list) {
                logger.info("Pay Id = {}", pay.id)
                logger.info("Current Pay = {}", pay)
            }
        }
    }

    /*
    * Spring Batch에서는 offset과 limit을 PageSize에 맞게 자동으로 생성해 줍니다.
    * 다만 각 쿼리는 개별적으로 실행한다는 점을 유의해야합니다.
    * 각 페이지마다 새로운 쿼리를 실행하므로 페이징시 결과를 정렬하는 것이 중요합니다.
    * */

    @Bean
    fun createQueryProvider(): PagingQueryProvider {
        val queryProvider = SqlPagingQueryProviderFactoryBean()
        queryProvider.setDataSource(dataSource)  // Database에 맞는 PagingQueryProvider를 선택하기 위해
        queryProvider.setSelectClause("id, amount, tx_name, tx_date_time")
        queryProvider.setFromClause("from pay")
        queryProvider.setWhereClause("where amount >= :amount")

        val sortKeys: MutableMap<String, Order> = HashMap(1)
        sortKeys["id"] = Order.ASCENDING

        queryProvider.setSortKeys(sortKeys)

        return queryProvider.getObject()
    }
}
