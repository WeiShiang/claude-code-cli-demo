package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.time.Instant;

public record ProductPriceChangedEvent(
        ProductId productId,
        Money oldPrice,
        Money newPrice,
        Instant occurredAt
) implements DomainEvent {
}
