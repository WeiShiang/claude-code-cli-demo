package com.example.claudecodeclidemo.catalog.domain.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) { super(message); }
}
