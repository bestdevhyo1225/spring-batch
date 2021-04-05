package com.hs.batch.reader

import com.hs.batch.reader.options.QuerydslNoOffsetPagingOptions
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.util.ClassUtils
import org.springframework.util.CollectionUtils
import java.util.function.Function
import javax.persistence.EntityManagerFactory

class QuerydslNoOffsetPagingItemReader<T>(
    entityManagerFactory: EntityManagerFactory,
    queryFunction: Function<JPAQueryFactory, JPAQuery<T>>,
    pageSize: Int,
    noOffsetPagingOptions: QuerydslNoOffsetPagingOptions<T, Long>,
) : QuerydslPagingItemReader<T>(entityManagerFactory, queryFunction, pageSize) {

    private val noOffsetPagingOptions: QuerydslNoOffsetPagingOptions<T, Long>

    init {
        setName(ClassUtils.getShortName(QuerydslNoOffsetPagingItemReader::class.java))
        this.noOffsetPagingOptions = noOffsetPagingOptions
    }

    override fun doReadPage() {
        clearPersistenceContext()

        val query: JPAQuery<T> = createQuery().limit(pageSize.toLong())

        initResults()
        fetchQuery(query)
        resetCurrentKey()
    }

    override fun createQuery(): JPAQuery<T> {
        val jpaQueryFactory = JPAQueryFactory(entityManager)
        val query: JPAQuery<T> = queryFunction.apply(jpaQueryFactory)

        noOffsetPagingOptions.initCurrentKey(query)
        return noOffsetPagingOptions.createQuery(query)
    }

    private fun resetCurrentKey() {
        if (isNotEmptyResults()) {
            val item: T = results[results.size - 1]
            noOffsetPagingOptions.resetCurrnetKey(item)
        }
    }

    private fun isNotEmptyResults(): Boolean {
        return !CollectionUtils.isEmpty(results) && results[0] != null
    }
}

