package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;

import java.math.BigDecimal;
import java.util.UUID;

record ProductResponse(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        String currency,
        UUID categoryId,
        String status
) {
    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId().value(),
                product.getName(),
                product.getSku().value(),
                product.getPrice().amount(),
                product.getPrice().currency().getCurrencyCode(),
                product.getCategoryId().value(),
                product.getStatus().name()
        );
    }
}
