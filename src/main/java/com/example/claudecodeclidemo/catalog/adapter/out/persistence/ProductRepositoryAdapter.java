package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.*;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Optional;

@Component
class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;

    ProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product product) {
        var entity = toJpa(product);
        var saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Product> findBySku(Sku sku) {
        return jpa.findBySku(sku.value()).map(this::toDomain);
    }

    private ProductJpaEntity toJpa(Product p) {
        return new ProductJpaEntity(
                p.getId().value(),
                p.getName(),
                p.getSku().value(),
                p.getPrice().amount(),
                p.getPrice().currency().getCurrencyCode(),
                p.getCategoryId().value(),
                p.getStatus().name()
        );
    }

    private Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(
                new ProductId(e.id),
                e.name,
                new Sku(e.sku),
                new Money(e.priceAmount, Currency.getInstance(e.priceCurrency)),
                new CategoryId(e.categoryId),
                ProductStatus.valueOf(e.status)
        );
    }
}
