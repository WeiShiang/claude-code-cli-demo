package com.example.claudecodeclidemo.catalog.domain.exception;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;

public class InvalidProductStateTransitionException extends RuntimeException {
    public InvalidProductStateTransitionException(ProductStatus from, ProductStatus to) {
        super("Cannot transition product status from " + from + " to " + to);
    }
}
