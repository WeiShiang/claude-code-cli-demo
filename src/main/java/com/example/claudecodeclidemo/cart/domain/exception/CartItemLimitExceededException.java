package com.example.claudecodeclidemo.cart.domain.exception;

public class CartItemLimitExceededException extends RuntimeException {
    public CartItemLimitExceededException() {
        super("Cart cannot have more than 50 items");
    }
}
