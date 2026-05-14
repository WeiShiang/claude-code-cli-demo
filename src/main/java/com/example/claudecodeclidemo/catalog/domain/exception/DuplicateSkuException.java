package com.example.claudecodeclidemo.catalog.domain.exception;

import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

public class DuplicateSkuException extends RuntimeException {
    public DuplicateSkuException(Sku sku) {
        super("SKU already exists: " + sku.value());
    }
}
