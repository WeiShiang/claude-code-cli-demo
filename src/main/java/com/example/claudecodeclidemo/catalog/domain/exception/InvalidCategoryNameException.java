package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidCategoryNameException extends RuntimeException {
    public InvalidCategoryNameException(String message) { super(message); }
}
