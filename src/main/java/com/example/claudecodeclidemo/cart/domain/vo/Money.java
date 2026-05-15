package com.example.claudecodeclidemo.cart.domain.vo;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("amount and currency must not be null");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money m)) return false;
        return amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
    }

    @Override public int hashCode() { return Objects.hash(amount.stripTrailingZeros(), currency); }
    @Override public String toString() { return amount + " " + currency.getCurrencyCode(); }
}
