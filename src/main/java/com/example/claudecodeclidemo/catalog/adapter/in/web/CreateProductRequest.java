package com.example.claudecodeclidemo.catalog.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

record CreateProductRequest(
        @NotBlank String name,
        @NotBlank String sku,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank String currency,
        @NotNull UUID categoryId
) {}
