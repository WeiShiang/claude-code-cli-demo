package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.StockDepletedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidDeductAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidReleaseAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockQuantityException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidStockReservationException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

import java.time.Instant;

public class Stock extends AggregateRoot {

    private final ProductId productId;
    private final Sku sku;
    private int quantity;
    private int reserved;

    private Stock(ProductId productId, Sku sku, int quantity, int reserved) {
        this.productId = productId;
        this.sku = sku;
        this.quantity = quantity;
        this.reserved = reserved;
    }

    public static Stock create(ProductId productId, Sku sku, int quantity) {
        if (quantity < 0) throw new InvalidStockQuantityException(quantity);
        return new Stock(productId, sku, quantity, 0);
    }

    public void reserve(int amount) {
        if (amount <= 0) throw new InvalidStockReservationException(amount);
        if (availableQuantity() < amount) throw new InsufficientStockException(productId, amount);
        this.reserved += amount;
    }

    public void release(int amount) {
        if (amount <= 0 || amount > reserved) throw new InvalidReleaseAmountException(amount, reserved);
        this.reserved -= amount;
    }

    public void deduct(int amount) {
        if (amount <= 0 || amount > reserved) throw new InvalidDeductAmountException(amount, reserved);
        this.quantity -= amount;
        this.reserved -= amount;
        if (this.quantity == 0) {
            registerEvent(new StockDepletedEvent(productId, sku, Instant.now()));
        }
    }

    public int availableQuantity() {
        return quantity - reserved;
    }

    public ProductId getProductId() { return productId; }
    public Sku getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public int getReserved() { return reserved; }
}
