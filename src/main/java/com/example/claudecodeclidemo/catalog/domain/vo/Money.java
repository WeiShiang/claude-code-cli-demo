package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidPriceException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null) throw new InvalidPriceException("amount cannot be null");
        if (currency == null) throw new InvalidPriceException("currency cannot be null");
        if (amount.signum() < 0) throw new InvalidPriceException("amount cannot be negative");
    }
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros().toPlainString(), currency);
    }
}
