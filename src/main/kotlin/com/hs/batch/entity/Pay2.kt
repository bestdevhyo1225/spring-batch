package com.hs.batch.entity

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
class Pay2(amount: Long, txName: String, txDateTime: String) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    var amount: Long = amount
        protected set

    @Column(nullable = false)
    var txName: String = txName
        protected set

    @Column(nullable = false)
    var txDateTime: LocalDateTime = LocalDateTime.parse(txDateTime, FORMATTER)
        protected set

    override fun toString() = kotlinToString(properties = toStringProperties)
    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)
    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val equalsAndHashCodeProperties = arrayOf(Pay2::id)
        private val toStringProperties = arrayOf(
            Pay2::id,
            Pay2::amount,
            Pay2::txName,
            Pay2::txDateTime,
        )
    }
}
