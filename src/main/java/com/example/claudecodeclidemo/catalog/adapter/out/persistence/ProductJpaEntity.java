package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
class ProductJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String sku;

    @Column(nullable = false)
    BigDecimal priceAmount;

    @Column(nullable = false, length = 3)
    String priceCurrency;

    @Column(nullable = false)
    UUID categoryId;

    @Column(nullable = false)
    String status;

    protected ProductJpaEntity() {}

    ProductJpaEntity(UUID id, String name, String sku,
                     BigDecimal priceAmount, String priceCurrency,
                     UUID categoryId, String status) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
        this.categoryId = categoryId;
        this.status = status;
    }
}
