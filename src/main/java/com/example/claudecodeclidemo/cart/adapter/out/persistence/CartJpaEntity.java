package com.example.claudecodeclidemo.cart.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
class CartJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false, unique = true)
    UUID userId;

    @Column(nullable = false)
    Instant updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<CartItemJpaEntity> items = new ArrayList<>();

    protected CartJpaEntity() {}

    CartJpaEntity(UUID id, UUID userId, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.updatedAt = updatedAt;
    }
}
