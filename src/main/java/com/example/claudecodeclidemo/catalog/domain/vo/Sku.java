package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;

import java.util.regex.Pattern;

public record Sku(String value) {

    private static final Pattern PATTERN = Pattern.compile("[A-Z0-9-]{4,20}");

    public Sku {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new InvalidSkuException(value);
        }
    }
}
