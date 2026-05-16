package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;

public record Sku(String value) {

    private static final java.util.regex.Pattern PATTERN =
            java.util.regex.Pattern.compile("[A-Z0-9-]{4,20}");

    public Sku {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new InvalidSkuException(value);
        }
    }
}
