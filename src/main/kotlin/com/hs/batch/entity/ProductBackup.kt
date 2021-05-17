package com.hs.batch.entity

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import javax.persistence.*

@Entity
class ProductBackup(name: String, amounts: Int) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var amounts: Int = amounts
        protected set

    override fun toString() = kotlinToString(properties = toStringProperties)
    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)
    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(ProductBackup::id)
        private val toStringProperties = arrayOf(ProductBackup::id, ProductBackup::name, ProductBackup::amounts)
    }
}
