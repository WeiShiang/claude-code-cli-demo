package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;

public record Sku(String value) {
    public Sku {
        if (value == null || value.isBlank()) {
            throw new InvalidSkuException("sku cannot be blank");
        }
    }
    public static Sku of(String value) { return new Sku(value); }
}
