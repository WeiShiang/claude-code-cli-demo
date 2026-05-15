package com.example.claudecodeclidemo.cart.domain.exception;

import com.example.claudecodeclidemo.cart.domain.vo.UserId;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(UserId userId) {
        super("Cart not found for user: " + userId.value());
    }
}
