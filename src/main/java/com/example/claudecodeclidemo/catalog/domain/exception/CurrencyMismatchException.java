package com.example.claudecodeclidemo.catalog.domain.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String message) { super(message); }
}
