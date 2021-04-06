package com.hs.batch.entity

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import javax.persistence.*

@Entity
class Product(name: String, amounts: Int) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var amounts: Int = amounts
        protected set

    @OneToMany(mappedBy = "product", cascade = [CascadeType.PERSIST])
    val productImages: MutableList<ProductImage> = mutableListOf()

    fun addProductImage(productImage: ProductImage) {
        productImages.add(productImage)
        productImage.changeProduct(this)
    }

    override fun toString() = kotlinToString(properties = toStringProperties)
    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)
    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(Product::id)
        private val toStringProperties = arrayOf(Product::id, Product::name, Product::amounts)

        fun create(name: String, amounts: Int, productImages: List<ProductImage>): Product {
            val product = Product(name = name, amounts = amounts)

            productImages.forEach { productImage -> product.addProductImage(productImage = productImage) }

            return product
        }
    }
}
