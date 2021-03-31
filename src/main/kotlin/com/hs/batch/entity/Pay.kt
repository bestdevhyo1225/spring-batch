package com.hs.batch.entity

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.*


@Entity
class Pay(amount: Long, txName: String, txDateTime: String) {

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

    fun setamount (amount: Long) {
        this.amount = amount
    }

    fun settxName (txName: String) {
        this.txName = txName
    }

    override fun toString() = kotlinToString(properties = toStringProperties)
    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)
    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val equalsAndHashCodeProperties = arrayOf(Pay::id)
        private val toStringProperties = arrayOf(
            Pay::id,
            Pay::amount,
            Pay::txName,
            Pay::txDateTime,
        )
    }
}
