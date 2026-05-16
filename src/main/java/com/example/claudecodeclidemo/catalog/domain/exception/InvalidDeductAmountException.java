package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidDeductAmountException extends RuntimeException {
    public InvalidDeductAmountException(int amount, int reserved) {
        super("Deduct amount (" + amount + ") must be > 0 and <= reserved (" + reserved + ")");
    }
}
