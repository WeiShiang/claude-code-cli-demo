package com.example.claudecodeclidemo.catalog.domain.exception;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(ProductId productId) {
        super("Product not found: " + productId);
    }
}
