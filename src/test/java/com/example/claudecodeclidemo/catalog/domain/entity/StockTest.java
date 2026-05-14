package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.StockDepletedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidDeductAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidReleaseAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockQuantityException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockReservationException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    private ProductId productId;

    @BeforeEach
    void setUp() {
        productId = new ProductId(UUID.randomUUID());
    }

    // ── Invariant S-1: quantity ≥ 0 ──────────────────────────────────

    @Test
    void 負數庫存建立時拋出InvalidStockQuantityException() {
        assertThatThrownBy(() -> Stock.create(productId, -1))
                .isInstanceOf(InvalidStockQuantityException.class);
    }

    @Test
    void 零庫存可建立() {
        var stock = Stock.create(productId, 0);
        assertThat(stock.getQuantity()).isEqualTo(0);
        assertThat(stock.getReserved()).isEqualTo(0);
    }

    @Test
    void 正數庫存可建立() {
        var stock = Stock.create(productId, 10);
        assertThat(stock.getQuantity()).isEqualTo(10);
    }

    // ── Invariant S-2 + S-3: reserve 操作 ────────────────────────────

    @Test
    void 可用庫存足夠時可以保留() {
        var stock = Stock.create(productId, 10);
        stock.reserve(3);
        assertThat(stock.getReserved()).isEqualTo(3);
        assertThat(stock.availableQuantity()).isEqualTo(7);
    }

    @Test
    void 可用庫存不足時保留拋出InsufficientStockException() {
        var stock = Stock.create(productId, 5);
        assertThatThrownBy(() -> stock.reserve(6))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void 零庫存保留任何數量都拋出InsufficientStockException() {
        var stock = Stock.create(productId, 0);
        assertThatThrownBy(() -> stock.reserve(1))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void reserve數量為零時拋出InvalidStockReservationException() {
        var stock = Stock.create(productId, 10);
        assertThatThrownBy(() -> stock.reserve(0))
                .isInstanceOf(InvalidStockReservationException.class);
    }

    @Test
    void reserve負數時拋出InvalidStockReservationException() {
        var stock = Stock.create(productId, 10);
        assertThatThrownBy(() -> stock.reserve(-1))
                .isInstanceOf(InvalidStockReservationException.class);
    }

    // ── Invariant S-5: release ≤ reserved ────────────────────────────

    @Test
    void 保留量足夠時可以釋放() {
        var stock = Stock.create(productId, 10);
        stock.reserve(5);
        stock.release(3);
        assertThat(stock.getReserved()).isEqualTo(2);
        assertThat(stock.availableQuantity()).isEqualTo(8);
    }

    @Test
    void 釋放超過保留量時拋出InvalidReleaseAmountException() {
        var stock = Stock.create(productId, 10);
        stock.reserve(3);
        assertThatThrownBy(() -> stock.release(4))
                .isInstanceOf(InvalidReleaseAmountException.class);
    }

    @Test
    void 無保留量時釋放拋出InvalidReleaseAmountException() {
        var stock = Stock.create(productId, 10);
        assertThatThrownBy(() -> stock.release(1))
                .isInstanceOf(InvalidReleaseAmountException.class);
    }

    // ── Invariant S-4: deduct ≤ reserved ─────────────────────────────

    @Test
    void 保留量足夠時可以扣除() {
        var stock = Stock.create(productId, 10);
        stock.reserve(5);
        stock.deduct(5);
        assertThat(stock.getQuantity()).isEqualTo(5);
        assertThat(stock.getReserved()).isEqualTo(0);
    }

    @Test
    void 扣除超過保留量時拋出InvalidDeductAmountException() {
        var stock = Stock.create(productId, 10);
        stock.reserve(3);
        assertThatThrownBy(() -> stock.deduct(4))
                .isInstanceOf(InvalidDeductAmountException.class);
    }

    @Test
    void 無保留量時扣除拋出InvalidDeductAmountException() {
        var stock = Stock.create(productId, 10);
        assertThatThrownBy(() -> stock.deduct(1))
                .isInstanceOf(InvalidDeductAmountException.class);
    }

    // ── availableQuantity ─────────────────────────────────────────────

    @Test
    void 可用庫存等於總量減保留量() {
        var stock = Stock.create(productId, 10);
        stock.reserve(3);
        assertThat(stock.availableQuantity()).isEqualTo(7);
    }

    // ── Domain Event: StockDepletedEvent ──────────────────────────────

    @Test
    void 扣除後庫存歸零時發布StockDepletedEvent() {
        var stock = Stock.create(productId, 3);
        stock.reserve(3);
        stock.deduct(3);

        assertThat(stock.getDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(StockDepletedEvent.class);

        var event = (StockDepletedEvent) stock.getDomainEvents().get(0);
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void 扣除後庫存未歸零時不發布StockDepletedEvent() {
        var stock = Stock.create(productId, 10);
        stock.reserve(3);
        stock.deduct(3);
        assertThat(stock.getDomainEvents()).isEmpty();
    }
}
