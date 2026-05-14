package com.example.claudecodeclidemo.catalog.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

record CreateProductRequest(
        String name,
        String sku,
        BigDecimal price,
        String currencyCode,
        UUID categoryId
) {}
