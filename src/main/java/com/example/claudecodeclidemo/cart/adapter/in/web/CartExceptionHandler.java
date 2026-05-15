package com.example.claudecodeclidemo.cart.adapter.in.web;

import com.example.claudecodeclidemo.cart.domain.exception.CartItemLimitExceededException;
import com.example.claudecodeclidemo.cart.domain.exception.CartItemNotFoundException;
import com.example.claudecodeclidemo.cart.domain.exception.CartNotFoundException;
import com.example.claudecodeclidemo.cart.domain.exception.EmptyCartException;
import com.example.claudecodeclidemo.cart.domain.exception.InvalidQuantityException;
import com.example.claudecodeclidemo.cart.domain.exception.ProductNotAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class CartExceptionHandler {

    @ExceptionHandler(InvalidQuantityException.class)
    ProblemDetail handleInvalidQuantity(InvalidQuantityException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Quantity");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler({EmptyCartException.class, CartItemLimitExceededException.class,
            ProductNotAvailableException.class})
    ProblemDetail handleDomainViolation(RuntimeException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Cart Rule Violation");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler({CartNotFoundException.class, CartItemNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}
