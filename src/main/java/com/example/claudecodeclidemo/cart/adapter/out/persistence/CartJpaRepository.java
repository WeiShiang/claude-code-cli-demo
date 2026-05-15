package com.example.claudecodeclidemo.cart.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {
    Optional<CartJpaEntity> findByUserId(UUID userId);
}
