package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.time.Instant;

public record ProductCreatedEvent(
        ProductId productId,
        Sku sku,
        Money price,
        Instant occurredAt
) implements DomainEvent {
}
