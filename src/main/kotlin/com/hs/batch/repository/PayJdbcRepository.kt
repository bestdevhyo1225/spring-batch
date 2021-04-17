package com.hs.batch.repository

import com.hs.batch.entity.Pay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Timestamp

@Repository
class PayJdbcRepository(
    @Value("\${batch-size}")
    private val batchSize: Int,
    private val jdbcTemplate: JdbcTemplate
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun saveAll(entityList: MutableList<Pay>) {
        val subEntityList: MutableList<Pay> = mutableListOf()
        var batchCount = 0

        for (i in 0 until entityList.size) {
            subEntityList.add(entityList[i])

            if ((i + 1) % batchSize == 0) {
                batchCount = batchInsert(batchCount, subEntityList)
            }
        }

        if (subEntityList.isNotEmpty()) {
            batchCount = batchInsert(batchCount, subEntityList)
        }

        logger.info("Entity total size = {}", entityList.size)
        logger.info("Entity batch count = {}", batchCount)
    }

    private fun batchInsert(batchCount: Int, subEntityList: MutableList<Pay>): Int {
        val bulkInsertQuery = "INSERT INTO pay (amount, tx_name, tx_date_time) VALUES (?, ?, ?)"

        jdbcTemplate.batchUpdate(bulkInsertQuery, object : BatchPreparedStatementSetter {
            override fun setValues(preparedStatement: PreparedStatement, index: Int) {
                preparedStatement.setLong(1, subEntityList[index].amount)
                preparedStatement.setString(2, subEntityList[index].txName)
                preparedStatement.setTimestamp(3, Timestamp.valueOf(subEntityList[index].txDateTime))
            }

            override fun getBatchSize(): Int {
                return subEntityList.size
            }
        })

        subEntityList.clear()

        return batchCount + 1
    }
}
