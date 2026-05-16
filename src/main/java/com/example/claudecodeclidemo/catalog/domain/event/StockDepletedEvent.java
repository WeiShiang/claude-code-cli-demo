package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.time.Instant;

public record StockDepletedEvent(
        ProductId productId,
        Instant occurredAt
) implements DomainEvent {
}
