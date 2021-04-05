package com.hs.batch.repository

import com.hs.batch.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long> {
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.productImages pi")
    fun findProductsWithFetchJoin(): List<Product>
}
