package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidProductNameException extends RuntimeException {
    public InvalidProductNameException(String message) { super(message); }
}
