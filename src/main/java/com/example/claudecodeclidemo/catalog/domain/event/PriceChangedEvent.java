package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PriceChangedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID productId,
        String sku,
        Money previousPrice,
        Money newPrice
) implements DomainEvent {}
