package com.example.claudecodeclidemo.catalog.domain.exception;

import com.example.claudecodeclidemo.catalog.domain.entity.ProductStatus;

public class InvalidProductStateTransitionException extends RuntimeException {
    public InvalidProductStateTransitionException(ProductStatus from, ProductStatus to) {
        super("Invalid product state transition: " + from + " -> " + to);
    }
}
