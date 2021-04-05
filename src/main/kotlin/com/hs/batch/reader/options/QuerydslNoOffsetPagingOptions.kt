package com.hs.batch.reader.options

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.jpa.impl.JPAQuery
import java.lang.reflect.Field

class QuerydslNoOffsetPagingOptions<T, N>(private val fieldOfNumber: NumberPath<N>) where N : Number, N : Comparable<*> {

    private var isFirstPage: Boolean = true
    private var currentKey: Long? = null

    fun initCurrentKey(query: JPAQuery<T>) {
        if (currentKey == null) {
            val cloneQuery: JPAQuery<T> = query.clone()

            currentKey = cloneQuery
                .select(fieldOfNumber.castToNum(Long::class.java))
                .orderBy(fieldOfNumber.asc())
                .fetchFirst()
        }
    }

    fun createQuery(query: JPAQuery<T>): JPAQuery<T> {
        if (currentKey == null) return query

        return query.where(fieldGoeOrGtCurrentKey())
    }

    private fun fieldGoeOrGtCurrentKey(): BooleanExpression {
        return if (isFirstPage) {
            isFirstPage = false
            fieldOfNumber.goe(currentKey)
        } else {
            fieldOfNumber.gt(currentKey)
        }
    }

    fun resetCurrnetKey(item: T) {
        currentKey = getFieldValue(item) as Long
    }

    private fun getFieldValue(item: T): Any {
        try {
            val filed: Field = item!!::class.java.getDeclaredField("id")
            filed.isAccessible = true // 필드에 접근 가능하도록 true로 설정한다.
            return filed.get(item)
        } catch (e: NoSuchFieldException) {
            throw IllegalArgumentException("Not Found or Not Access Field")
        } catch (e: IllegalAccessException) {
            throw IllegalArgumentException("Not Found or Not Access Field")
        }
    }
}
