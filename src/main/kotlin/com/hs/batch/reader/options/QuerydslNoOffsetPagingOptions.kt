package com.hs.batch.reader.options

import com.hs.batch.entity.QPay.pay
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQuery
import java.lang.reflect.Field

class QuerydslNoOffsetPagingOptions<T> {

    private var isFirstPage: Boolean = true
    private var currentKey: Long? = null

    fun initCurrentKey(query: JPAQuery<T>) {
        if (currentKey == null) {
            val cloneQuery: JPAQuery<T> = query.clone()

            currentKey = cloneQuery
                .select(pay.id)
                .from(pay)
                .orderBy(pay.id.asc())
                .fetchFirst()
        }
    }

    fun createQuery(query: JPAQuery<T>): JPAQuery<T> {
        if (currentKey == null) return query

        return query.where(payIdGoeOrGtCurrentKey())
    }

    private fun payIdGoeOrGtCurrentKey(): BooleanExpression {
        return if (isFirstPage) {
            isFirstPage = false
            pay.id.goe(currentKey)
        } else {
            pay.id.gt(currentKey)
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
