package com.example.claudecodeclidemo.catalog.domain.exception;

public class DuplicateSkuException extends RuntimeException {
    public DuplicateSkuException(String message) { super(message); }
}
