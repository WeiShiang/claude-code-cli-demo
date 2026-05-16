package com.example.claudecodeclidemo.catalog.domain.exception;

public class InvalidStockReservationException extends RuntimeException {
    public InvalidStockReservationException(int amount) {
        super("Reserve amount must be > 0, but was: " + amount);
    }
}
