package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidPriceException;

public record ListPrice(Money money) {
    public ListPrice {
        if (money == null) throw new InvalidPriceException("money cannot be null");
        if (money.amount().signum() <= 0) {
            throw new InvalidPriceException("listPrice must be greater than zero");
        }
    }
    public static ListPrice of(Money money) { return new ListPrice(money); }
}
