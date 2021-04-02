package com.hs.batch.reader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.util.ClassUtils
import org.springframework.util.CollectionUtils
import java.lang.Exception
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import java.util.HashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function


open class QuerydslPagingItemReader<T>(
    entityManagerFactory: EntityManagerFactory,
    queryFunction: Function<JPAQueryFactory, JPAQuery<T>>,
    pageSize: Int
) : AbstractPagingItemReader<T>() {

    private val jpaPropertyMap: Map<String, Any> = HashMap()
    private val entityManagerFactory: EntityManagerFactory
    private lateinit var entityManager: EntityManager
    protected val queryFunction: Function<JPAQueryFactory, JPAQuery<T>>

    /**
     * Reader의 트랜잭션격리 옵션
     * - false: 격리 시키지 않고, Chunk 트랜잭션에 의존한다
     * (hibernate.default_batch_fetch_size 옵션 사용가능)
     * - true: 격리 시킨다
     * (Reader 조회 결과를 삭제하고 다시 조회했을때 삭제된게 반영되고 조회되길 원할때 사용한다.)
     */
    private var transacted = true // default value

    init {
        this.setName(ClassUtils.getShortName(QuerydslPagingItemReader::class.java))
        this.entityManagerFactory = entityManagerFactory
        this.queryFunction = queryFunction
        this.pageSize = pageSize
    }

    fun setTransacted(transacted: Boolean) {
        this.transacted = transacted
    }

    override fun doOpen() {
        super.doOpen()
        entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap)
            ?: throw DataAccessResourceFailureException("Unable to obtain an EntityManager")
    }

    override fun doReadPage() {
        clearPersistenceContext()

        val query: JPAQuery<T> = createQuery()
            .limit(pageSize.toLong())
            .offset(page.toLong() * pageSize.toLong())

        initResults()

        fetchQuery(query)
    }

    protected fun clearPersistenceContext() {
        if (transacted) {
            entityManager.clear()
        }
    }

    protected fun createQuery(): JPAQuery<T> {
        val jpaQueryFactory = JPAQueryFactory(entityManager)
        return queryFunction.apply(jpaQueryFactory)
    }

    protected fun initResults() {
        if (CollectionUtils.isEmpty(results)) {
            results = CopyOnWriteArrayList()
        } else {
            results.clear()
        }
    }

    protected fun fetchQuery(query: JPAQuery<T>) {
        if (transacted) results.addAll(query.fetch())
        else {
            val queryResult: List<T> = query.fetch()
            for (entity in queryResult) {
                entityManager.detach(entity)
                results.add(entity)
            }
        }
    }

    override fun doJumpToPage(itemIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun doClose() {
        try {
            entityManager.close()
            super.doClose()
        } catch (e: Exception) {
            throw RuntimeException("Execute doClose Fail!")
        }
    }
}
