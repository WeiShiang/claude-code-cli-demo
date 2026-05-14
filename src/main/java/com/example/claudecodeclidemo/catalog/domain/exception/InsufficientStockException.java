package com.example.claudecodeclidemo.catalog.domain.exception;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(ProductId productId, int requested) {
        super("Insufficient stock for product " + productId + ", requested: " + requested);
    }
}
