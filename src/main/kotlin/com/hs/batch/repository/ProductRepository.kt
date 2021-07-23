package com.hs.batch.repository

import com.hs.batch.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ProductRepository : JpaRepository<Product, Long> {
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.productImages pi")
    fun findProductsWithFetchJoin(): List<Product>

    @Query(value = "SELECT MAX(p.id) FROM Product p WHERE p.createDate BETWEEN :startDate AND :endDate")
    fun findMaxId(startDate: LocalDate, endDate: LocalDate): Long

    @Query(value = "SELECT MIN(p.id) FROM Product p WHERE p.createDate BETWEEN :startDate AND :endDate")
    fun findMinId(startDate: LocalDate, endDate: LocalDate): Long
}
