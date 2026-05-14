package com.example.claudecodeclidemo.catalog.domain.vo;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException() {
        super("Cannot operate on Money with different currencies");
    }
}
