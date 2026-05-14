package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidStockQuantityException extends RuntimeException {
    public InvalidStockQuantityException(int quantity) {
        super("Stock quantity must be >= 0, but was: " + quantity);
    }
}
