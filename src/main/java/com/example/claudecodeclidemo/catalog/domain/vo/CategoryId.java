package com.example.claudecodeclidemo.catalog.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record CategoryId(UUID value) {
    public CategoryId {
        Objects.requireNonNull(value, "value");
    }
    public static CategoryId generate() { return new CategoryId(UUID.randomUUID()); }
    public static CategoryId of(UUID value) { return new CategoryId(value); }
}
