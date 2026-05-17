package com.example.claudecodeclidemo.catalog.domain.exception;

public class MissingPriceException extends RuntimeException {
    public MissingPriceException(String message) { super(message); }
}
