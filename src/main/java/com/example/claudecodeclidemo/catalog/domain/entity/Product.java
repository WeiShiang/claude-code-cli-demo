package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.ProductCreatedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductPriceChangedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

import java.time.Instant;

public class Product extends AggregateRoot {

    private final ProductId id;
    private String name;
    private final Sku sku;
    private Money price;
    private final CategoryId categoryId;
    private ProductStatus status;

    private Product(ProductId id, String name, Sku sku, Money price,
                    CategoryId categoryId, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.categoryId = categoryId;
        this.status = status;
    }

    public static Product create(String name, Sku sku, Money price, CategoryId categoryId) {
        if (name == null || name.isBlank()) throw new InvalidProductNameException();
        var product = new Product(ProductId.generate(), name, sku, price, categoryId, ProductStatus.ACTIVE);
        product.registerEvent(new ProductCreatedEvent(product.id, product.sku, product.price, Instant.now()));
        return product;
    }

    public void activate() {
        if (status == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStateTransitionException(status, ProductStatus.ACTIVE);
        }
        this.status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public void changePrice(Money newPrice) {
        var oldPrice = this.price;
        this.price = newPrice;
        registerEvent(new ProductPriceChangedEvent(this.id, oldPrice, newPrice, Instant.now()));
    }

    public ProductId getId() { return id; }
    public String getName() { return name; }
    public Sku getSku() { return sku; }
    public Money getPrice() { return price; }
    public CategoryId getCategoryId() { return categoryId; }
    public ProductStatus getStatus() { return status; }
}
