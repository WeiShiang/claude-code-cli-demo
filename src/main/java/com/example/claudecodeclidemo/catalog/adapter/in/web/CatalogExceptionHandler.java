package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.CurrencyMismatchException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidCategoryNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidPriceException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.MissingPriceException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductArchivedException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.claudecodeclidemo.catalog.adapter.in.web")
public class CatalogExceptionHandler {

    public record ErrorResponse(String code, String message) {}

    @ExceptionHandler({ProductNotFoundException.class, CategoryNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ExceptionHandler({DuplicateSkuException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ExceptionHandler({
            InvalidProductStateTransitionException.class,
            MissingPriceException.class,
            ProductArchivedException.class,
            CurrencyMismatchException.class,
            InvalidPriceException.class,
            InvalidSkuException.class,
            InvalidProductNameException.class,
            InvalidCategoryNameException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> badRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }
}
