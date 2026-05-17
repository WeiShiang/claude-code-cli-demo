package com.example.claudecodeclidemo.catalog.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record ProductId(UUID value) {
    public ProductId {
        Objects.requireNonNull(value, "value");
    }
    public static ProductId generate() { return new ProductId(UUID.randomUUID()); }
    public static ProductId of(UUID value) { return new ProductId(value); }
}
