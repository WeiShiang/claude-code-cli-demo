package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;

import java.math.BigDecimal;
import java.util.UUID;

record ProductResponse(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        String currencyCode,
        UUID categoryId,
        String status
) {
    static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId().value(),
                p.getName(),
                p.getSku().value(),
                p.getPrice().amount(),
                p.getPrice().currency().getCurrencyCode(),
                p.getCategoryId().value(),
                p.getStatus().name()
        );
    }
}
