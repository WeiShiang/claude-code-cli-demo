package com.example.claudecodeclidemo.catalog.domain.vo;

public class InvalidSkuException extends RuntimeException {
    public InvalidSkuException(String value) {
        super("Invalid SKU format: '" + value + "'. Must match [A-Z0-9-]{4,20}");
    }
}
