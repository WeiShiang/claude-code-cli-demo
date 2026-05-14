package com.example.claudecodeclidemo.catalog.domain.vo;

import java.math.BigDecimal;

public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException(BigDecimal amount) {
        super("Price must be >= 0, but was: " + amount);
    }
}
