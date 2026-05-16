package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.StockDepletedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidDeductAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidReleaseAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockQuantityException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockReservationException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.shared.domain.AggregateRoot;

import java.time.Instant;

public class Stock extends AggregateRoot<ProductId> {

    private final ProductId productId;
    private int quantity;
    private int reserved;

    private Stock(ProductId productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.reserved = 0;
    }

    public static Stock of(ProductId productId, int quantity) {
        if (quantity < 0) {
            throw new InvalidStockQuantityException(quantity);
        }
        return new Stock(productId, quantity);
    }

    public static Stock reconstitute(ProductId productId, int quantity, int reserved) {
        Stock stock = new Stock(productId, quantity);
        stock.reserved = reserved;
        return stock;
    }

    public void reserve(int amount) {
        if (amount <= 0) {
            throw new InvalidStockReservationException(amount);
        }
        if (availableQuantity() < amount) {
            throw new InsufficientStockException(availableQuantity(), amount);
        }
        reserved += amount;
    }

    public void release(int amount) {
        if (amount <= 0) throw new InvalidReleaseAmountException(amount, reserved);
        if (amount > reserved) throw new InvalidReleaseAmountException(amount, reserved);
        reserved -= amount;
    }

    public void deduct(int amount) {
        if (amount <= 0) throw new InvalidDeductAmountException(amount, reserved);
        if (amount > reserved) throw new InvalidDeductAmountException(amount, reserved);
        quantity -= amount;
        reserved -= amount;
        if (quantity == 0) {
            registerEvent(new StockDepletedEvent(productId, Instant.now()));
        }
    }

    public int availableQuantity() {
        return quantity - reserved;
    }

    @Override public ProductId getId() { return productId; }
    public ProductId getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public int getReserved() { return reserved; }
}
