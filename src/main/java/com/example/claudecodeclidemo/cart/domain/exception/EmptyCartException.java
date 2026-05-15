package com.example.claudecodeclidemo.cart.domain.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() {
        super("Cannot checkout an empty cart");
    }
}
