package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ProductUnpublishedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID productId,
        String sku,
        UnpublishReason reason
) implements DomainEvent {}
