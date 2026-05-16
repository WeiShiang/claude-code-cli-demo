package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.StockDepletedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidDeductAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidReleaseAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockQuantityException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockReservationException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    private static final ProductId PRODUCT_ID = new ProductId(UUID.randomUUID());

    // ── S-1: quantity 必須 >= 0 ───────────────────────────────────────────

    @Test
    void of_negativeQuantity_throwsInvalidStockQuantityException() {
        assertThatThrownBy(() -> Stock.of(PRODUCT_ID, -1))
                .isInstanceOf(InvalidStockQuantityException.class);
    }

    @Test
    void of_zeroQuantity_creates() {
        var stock = Stock.of(PRODUCT_ID, 0);
        assertThat(stock.getQuantity()).isZero();
    }

    @Test
    void of_positiveQuantity_creates() {
        var stock = Stock.of(PRODUCT_ID, 10);
        assertThat(stock.getQuantity()).isEqualTo(10);
        assertThat(stock.getReserved()).isZero();
    }

    // ── S-2: reserved >= 0 且 <= quantity ─────────────────────────────────

    @Test
    void reserve_moreThanAvailable_throwsInsufficientStockException() {
        var stock = Stock.of(PRODUCT_ID, 5);
        assertThatThrownBy(() -> stock.reserve(6))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void reserve_exactAvailable_succeeds() {
        var stock = Stock.of(PRODUCT_ID, 5);
        stock.reserve(5);
        assertThat(stock.getReserved()).isEqualTo(5);
        assertThat(stock.availableQuantity()).isZero();
    }

    // ── S-3: reserve 前確認可用庫存 ────────────────────────────────────────

    @Test
    void reserve_reducesAvailableQuantity() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(3);
        assertThat(stock.availableQuantity()).isEqualTo(7);
        assertThat(stock.getReserved()).isEqualTo(3);
    }

    @Test
    void reserve_whenNoAvailableStock_throwsInsufficientStockException() {
        var stock = Stock.of(PRODUCT_ID, 5);
        stock.reserve(5);
        assertThatThrownBy(() -> stock.reserve(1))
                .isInstanceOf(InsufficientStockException.class);
    }

    // ── S-4: deduct 數量不可超過 reserved ─────────────────────────────────

    @Test
    void deduct_moreThanReserved_throwsInvalidDeductAmountException() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(3);
        assertThatThrownBy(() -> stock.deduct(4))
                .isInstanceOf(InvalidDeductAmountException.class);
    }

    @Test
    void deduct_exactReserved_succeeds() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(3);
        stock.deduct(3);
        assertThat(stock.getQuantity()).isEqualTo(7);
        assertThat(stock.getReserved()).isZero();
    }

    @Test
    void deduct_partialReserved_succeeds() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(5);
        stock.deduct(3);
        assertThat(stock.getQuantity()).isEqualTo(7);
        assertThat(stock.getReserved()).isEqualTo(2);
    }

    // ── S-5: release 數量不可超過 reserved ────────────────────────────────

    @Test
    void release_moreThanReserved_throwsInvalidReleaseAmountException() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(3);
        assertThatThrownBy(() -> stock.release(4))
                .isInstanceOf(InvalidReleaseAmountException.class);
    }

    @Test
    void release_exactReserved_restoresAvailability() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(3);
        stock.release(3);
        assertThat(stock.getReserved()).isZero();
        assertThat(stock.availableQuantity()).isEqualTo(10);
    }

    // ── StockDepletedEvent 發布 ────────────────────────────────────────────

    @Test
    void deduct_stockGoesToZero_publishesStockDepletedEvent() {
        var stock = Stock.of(PRODUCT_ID, 3);
        stock.reserve(3);
        stock.deduct(3);
        assertThat(stock.getDomainEvents())
                .hasAtLeastOneElementOfType(StockDepletedEvent.class);
    }

    @Test
    void deduct_stockRemainsAboveZero_doesNotPublishStockDepletedEvent() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(3);
        stock.deduct(3);
        assertThat(stock.getDomainEvents())
                .noneMatch(e -> e instanceof StockDepletedEvent);
    }

    @Test
    void stockDepletedEvent_containsCorrectProductId() {
        var stock = Stock.of(PRODUCT_ID, 1);
        stock.reserve(1);
        stock.deduct(1);
        var event = (StockDepletedEvent) stock.getDomainEvents().stream()
                .filter(e -> e instanceof StockDepletedEvent)
                .findFirst().orElseThrow();
        assertThat(event.productId()).isEqualTo(PRODUCT_ID);
    }

    // ── availableQuantity ─────────────────────────────────────────────────

    @Test
    void availableQuantity_isQuantityMinusReserved() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(4);
        assertThat(stock.availableQuantity()).isEqualTo(6);
    }

    // ── 無效 reserve/deduct/release 數量 ──────────────────────────────────

    @Test
    void reserve_zeroAmount_throwsInvalidStockReservationException() {
        var stock = Stock.of(PRODUCT_ID, 10);
        assertThatThrownBy(() -> stock.reserve(0))
                .isInstanceOf(InvalidStockReservationException.class);
    }

    @Test
    void deduct_zeroAmount_throwsInvalidDeductAmountException() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(5);
        assertThatThrownBy(() -> stock.deduct(0))
                .isInstanceOf(InvalidDeductAmountException.class);
    }

    @Test
    void release_zeroAmount_throwsInvalidReleaseAmountException() {
        var stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(5);
        assertThatThrownBy(() -> stock.release(0))
                .isInstanceOf(InvalidReleaseAmountException.class);
    }
}
