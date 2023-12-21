package com.example.sync
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ProductService(
    private val repository: ProductRepository
) {

    fun save(product: Product): UUID {
        return repository
            .save(product)
    }
}