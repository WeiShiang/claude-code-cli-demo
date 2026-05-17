package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.Attribute;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.ListPrice;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QueryProductUseCase {
    ProductView findById(ProductId productId);
    ProductView findBySku(Sku sku);
    List<ProductView> findAll();

    record ProductView(
            ProductId id,
            Sku sku,
            String name,
            String description,
            Optional<ListPrice> listPrice,
            Set<CategoryId> categoryIds,
            List<Attribute> attributes,
            List<String> mediaUrls,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
