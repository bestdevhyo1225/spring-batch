package com.hs.batch.reader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.util.ClassUtils
import java.util.function.Function
import javax.persistence.EntityManagerFactory

class QuerydslNoOffsetPagingItemReader<T>(
    entityManagerFactory: EntityManagerFactory,
    queryFunction: Function<JPAQueryFactory, JPAQuery<T>>,
    pageSize: Int
) : QuerydslPagingItemReader<T>(entityManagerFactory, queryFunction, pageSize) {

    init {
        setName(ClassUtils.getShortName(QuerydslNoOffsetPagingItemReader::class.java))
    }
}
