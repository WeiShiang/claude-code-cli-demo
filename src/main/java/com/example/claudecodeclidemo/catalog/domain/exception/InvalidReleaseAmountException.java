package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidReleaseAmountException extends RuntimeException {
    public InvalidReleaseAmountException(int amount, int reserved) {
        super("Release amount (" + amount + ") must be > 0 and <= reserved (" + reserved + ")");
    }
}
