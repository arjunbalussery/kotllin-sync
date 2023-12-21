package com.example.sync

import com.example.generated.Tables.FLYWAY_SCHEMA_HISTORY
import com.example.generated.Tables.PRODUCT
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProductRepository(
    private val jooq: DSLContext
) {


    fun save(product: Product): UUID {
        jooq
            .insertInto(PRODUCT)
            .columns(PRODUCT.ID, PRODUCT.NAME)
            .values(product.id, product.name)
            .returning()
            .execute()
        return product.id
    }

}