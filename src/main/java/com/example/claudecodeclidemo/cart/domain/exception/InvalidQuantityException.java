package com.example.claudecodeclidemo.cart.domain.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(int value) {
        super("Quantity must be >= 1, but was: " + value);
    }
}
