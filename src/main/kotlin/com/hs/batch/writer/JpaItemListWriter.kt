package com.hs.batch.writer

import org.springframework.batch.item.database.JpaItemWriter

class JpaItemListWriter<T>(private val jpaItemWriter: JpaItemWriter<T>) : JpaItemWriter<List<T>>() {

    override fun write(items: MutableList<out List<T>>) {
        val totalList: MutableList<T> = mutableListOf()

        for (list: List<T> in items) {
            totalList.addAll(list)
        }

        jpaItemWriter.write(totalList)
    }
}
