package com.example.claudecodeclidemo.cart.domain.exception;

import com.example.claudecodeclidemo.cart.domain.vo.ProductId;

public class ProductNotAvailableException extends RuntimeException {
    public ProductNotAvailableException(ProductId productId) {
        super("Product is not available: " + productId);
    }
}
