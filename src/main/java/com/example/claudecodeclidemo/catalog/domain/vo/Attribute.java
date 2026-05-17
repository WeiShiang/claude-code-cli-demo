package com.example.claudecodeclidemo.catalog.domain.vo;

public record Attribute(String key, String value) {
    public Attribute {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("attribute key cannot be blank");
        if (value == null) throw new IllegalArgumentException("attribute value cannot be null");
    }
    public static Attribute of(String key, String value) { return new Attribute(key, value); }
}
