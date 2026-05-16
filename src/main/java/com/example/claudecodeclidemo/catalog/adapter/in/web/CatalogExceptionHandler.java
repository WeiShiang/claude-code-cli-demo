package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidDeductAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidPriceException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidReleaseAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockReservationException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class CatalogExceptionHandler {

    record ErrorResponse(String message) {}

    @ExceptionHandler({ProductNotFoundException.class, CategoryNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFound(RuntimeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleConflict(DuplicateSkuException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({InsufficientStockException.class, InvalidStockReservationException.class,
                       InvalidDeductAmountException.class, InvalidReleaseAmountException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ErrorResponse handleStockErrors(RuntimeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({InvalidSkuException.class, InvalidPriceException.class,
                       InvalidProductNameException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleDomainValidation(RuntimeException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
