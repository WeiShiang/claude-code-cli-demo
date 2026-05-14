package com.example.claudecodeclidemo.catalog.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void 正常金額可建立() {
        var money = new Money(new BigDecimal("100.00"), TWD);
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(money.currency()).isEqualTo(TWD);
    }

    @Test
    void 零元可建立() {
        var money = new Money(BigDecimal.ZERO, TWD);
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-0.01", "-100"})
    void 負數金額拋出InvalidPriceException(String amount) {
        assertThatThrownBy(() -> new Money(new BigDecimal(amount), TWD))
                .isInstanceOf(InvalidPriceException.class);
    }

    @Test
    void 相同金額與幣別的Money相等() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("100"), TWD);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void 不同金額的Money不相等() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("200"), TWD);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void 不同幣別的Money不相等() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("100"), USD);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void 相同幣別可以相加() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("50"), TWD);
        assertThat(a.add(b)).isEqualTo(new Money(new BigDecimal("150"), TWD));
    }

    @Test
    void 不同幣別相加拋出例外() {
        var twd = new Money(new BigDecimal("100"), TWD);
        var usd = new Money(new BigDecimal("100"), USD);
        assertThatThrownBy(() -> twd.add(usd))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void null金額拋出IllegalArgumentException() {
        assertThatThrownBy(() -> new Money(null, TWD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null幣別拋出IllegalArgumentException() {
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toString包含金額和幣別代碼() {
        var money = new Money(new BigDecimal("100"), TWD);
        assertThat(money.toString()).contains("100").contains("TWD");
    }

    @Test
    void 與非Money物件不相等() {
        var money = new Money(new BigDecimal("100"), TWD);
        assertThat(money).isNotEqualTo("100 TWD");
    }
}
