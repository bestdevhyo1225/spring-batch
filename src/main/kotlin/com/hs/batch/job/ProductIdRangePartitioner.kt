package com.hs.batch.job

import com.hs.batch.repository.ProductRepository
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import java.time.LocalDate

class ProductIdRangePartitioner(
    private val productRepository: ProductRepository,
    private val startDate: LocalDate,
    private val endDate: LocalDate
) : Partitioner {

    override fun partition(gridSize: Int): MutableMap<String, ExecutionContext> {
        val minId: Long = productRepository.findMinId(startDate = startDate, endDate = endDate)
        val maxId: Long = productRepository.findMaxId(startDate = startDate, endDate = endDate)
        val targetSize: Long = (maxId - minId) / gridSize + 1

        val result: MutableMap<String, ExecutionContext> = mutableMapOf()

        var number = 0
        var start: Long = minId
        var end: Long = start + targetSize - 1

        while (start <= end) {
            val executionContext = ExecutionContext()

            result["partition-$number"] = executionContext

            if (end >= maxId) end = maxId

            executionContext.putLong("minId", start)
            executionContext.putLong("maxId", end)

            start += targetSize
            end += targetSize

            number++
        }

        return result
    }
}
