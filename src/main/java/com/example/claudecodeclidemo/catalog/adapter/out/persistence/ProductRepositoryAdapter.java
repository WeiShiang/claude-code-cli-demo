package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Optional;

@Component
class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        jpaRepository.save(toJpa(product));
        return product;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return jpaRepository.existsBySku(sku.value());
    }

    private ProductJpaEntity toJpa(Product p) {
        return new ProductJpaEntity(
                p.getId().value(),
                p.getName(),
                p.getSku().value(),
                p.getPrice().amount(),
                p.getPrice().currency().getCurrencyCode(),
                p.getCategoryId().value(),
                p.getStatus()
        );
    }

    private Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(
                new ProductId(e.getId()),
                e.getName(),
                new Sku(e.getSku()),
                new Money(e.getPriceAmount(), Currency.getInstance(e.getPriceCurrency())),
                new CategoryId(e.getCategoryId()),
                e.getStatus()
        );
    }
}
