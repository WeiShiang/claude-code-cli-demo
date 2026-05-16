package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
class ProductJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    protected ProductJpaEntity() {}

    ProductJpaEntity(UUID id, String name, String sku, BigDecimal priceAmount,
                     String priceCurrency, UUID categoryId, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
        this.categoryId = categoryId;
        this.status = status;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    String getSku() { return sku; }
    BigDecimal getPriceAmount() { return priceAmount; }
    String getPriceCurrency() { return priceCurrency; }
    UUID getCategoryId() { return categoryId; }
    ProductStatus getStatus() { return status; }
}
