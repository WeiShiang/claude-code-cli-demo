package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.ProductCreatedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductPriceChangedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import com.example.claudecodeclidemo.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Product extends AggregateRoot<ProductId> {

    private final ProductId id;
    private final String name;
    private final Sku sku;
    private Money price;
    private final CategoryId categoryId;
    private ProductStatus status;

    private Product(ProductId id, String name, Sku sku, Money price, CategoryId categoryId) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.categoryId = categoryId;
        this.status = ProductStatus.ACTIVE;
    }

    public static Product create(String name, Sku sku, Money price, CategoryId categoryId) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductNameException();
        }
        var product = new Product(new ProductId(UUID.randomUUID()), name, sku, price, categoryId);
        product.registerEvent(new ProductCreatedEvent(product.id, sku, price, Instant.now()));
        return product;
    }

    public static Product reconstitute(ProductId id, String name, Sku sku, Money price, CategoryId categoryId, ProductStatus status) {
        var product = new Product(id, name, sku, price, categoryId);
        product.status = status;
        return product;
    }

    public void activate() {
        if (status == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStateTransitionException(status, ProductStatus.ACTIVE);
        }
        status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        status = ProductStatus.INACTIVE;
    }

    public void discontinue() {
        status = ProductStatus.DISCONTINUED;
    }

    public void changePrice(Money newPrice) {
        var oldPrice = this.price;
        this.price = newPrice;
        registerEvent(new ProductPriceChangedEvent(id, oldPrice, newPrice, Instant.now()));
    }

    @Override public ProductId getId() { return id; }
    public String getName() { return name; }
    public Sku getSku() { return sku; }
    public Money getPrice() { return price; }
    public CategoryId getCategoryId() { return categoryId; }
    public ProductStatus getStatus() { return status; }
}
