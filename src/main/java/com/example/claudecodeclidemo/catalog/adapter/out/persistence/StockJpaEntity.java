package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stocks")
class StockJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    UUID productId;

    @Column(nullable = false)
    String sku;

    @Column(nullable = false)
    int quantity;

    @Column(nullable = false)
    int reserved;

    protected StockJpaEntity() {}

    StockJpaEntity(UUID productId, String sku, int quantity, int reserved) {
        this.productId = productId;
        this.sku = sku;
        this.quantity = quantity;
        this.reserved = reserved;
    }
}
