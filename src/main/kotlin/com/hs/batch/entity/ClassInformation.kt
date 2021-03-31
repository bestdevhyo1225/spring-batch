package com.hs.batch.entity

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString

class ClassInformation(val id: Long? = null, val teacherName: String, val studentSize: Int) {
    override fun toString() = kotlinToString(properties = toStringProperties)
    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)
    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(ClassInformation::id)
        private val toStringProperties = arrayOf(
            ClassInformation::id,
            ClassInformation::teacherName,
            ClassInformation::studentSize
        )
    }
}
