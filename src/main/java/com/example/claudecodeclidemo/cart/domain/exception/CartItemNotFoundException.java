package com.example.claudecodeclidemo.cart.domain.exception;

import com.example.claudecodeclidemo.cart.domain.vo.ProductId;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(ProductId productId) {
        super("CartItem not found for product: " + productId.value());
    }
}
