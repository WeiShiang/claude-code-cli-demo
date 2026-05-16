package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stocks")
class StockJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reserved;

    protected StockJpaEntity() {}

    StockJpaEntity(UUID productId, int quantity, int reserved) {
        this.productId = productId;
        this.quantity = quantity;
        this.reserved = reserved;
    }

    UUID getProductId() { return productId; }
    int getQuantity() { return quantity; }
    int getReserved() { return reserved; }
}
