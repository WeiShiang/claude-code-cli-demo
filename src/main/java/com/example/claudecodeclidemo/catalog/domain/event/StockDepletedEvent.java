package com.example.claudecodeclidemo.catalog.domain.event;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

import java.time.Instant;

public record StockDepletedEvent(
        ProductId productId,
        Sku sku,
        Instant occurredAt
) {}
