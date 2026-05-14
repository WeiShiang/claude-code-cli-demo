package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class CatalogExceptionHandler {

    @ExceptionHandler(DuplicateSkuException.class)
    ProblemDetail handleDuplicateSku(DuplicateSkuException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Duplicate SKU");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler({ProductNotFoundException.class, CategoryNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler({InsufficientStockException.class, InvalidStockQuantityException.class,
            InvalidStockReservationException.class, InvalidDeductAmountException.class,
            InvalidReleaseAmountException.class})
    ProblemDetail handleStockViolation(RuntimeException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Stock Violation");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler({InvalidProductNameException.class, InvalidProductStateTransitionException.class})
    ProblemDetail handleDomainViolation(RuntimeException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Domain Violation");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}
