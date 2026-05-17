package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ProductPublishedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID productId,
        String sku,
        String name,
        Money listPrice,
        Set<UUID> categoryIds
) implements DomainEvent {}
