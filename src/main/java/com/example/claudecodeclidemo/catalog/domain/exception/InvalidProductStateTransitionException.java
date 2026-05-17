package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidProductStateTransitionException extends RuntimeException {
    public InvalidProductStateTransitionException(String message) { super(message); }
}
