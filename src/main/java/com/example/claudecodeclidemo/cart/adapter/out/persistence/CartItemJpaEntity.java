package com.example.claudecodeclidemo.cart.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
class CartItemJpaEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    CartJpaEntity cart;

    @Column(nullable = false)
    UUID productId;

    @Column(nullable = false)
    int quantity;

    @Column(nullable = false)
    BigDecimal unitPriceAmount;

    @Column(nullable = false, length = 3)
    String unitPriceCurrency;

    protected CartItemJpaEntity() {}

    CartItemJpaEntity(UUID id, CartJpaEntity cart, UUID productId,
                      int quantity, BigDecimal unitPriceAmount, String unitPriceCurrency) {
        this.id = id;
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceAmount = unitPriceAmount;
        this.unitPriceCurrency = unitPriceCurrency;
    }
}
