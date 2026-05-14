package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidProductNameException extends RuntimeException {
    public InvalidProductNameException() {
        super("Product name must not be blank");
    }
}
