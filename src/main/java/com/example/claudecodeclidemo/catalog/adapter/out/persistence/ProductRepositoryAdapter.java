package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.adapter.out.persistence.ProductJpaEntity.AttributeEmbeddable;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.Attribute;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.ListPrice;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Product product) {
        ProductJpaEntity entity = jpaRepository.findById(product.getId().value())
                .orElseGet(() -> new ProductJpaEntity(
                        product.getId().value(), product.getSku().value(),
                        product.getName(), product.getDescription(),
                        null, null, product.getStatus(),
                        product.getCreatedAt(), product.getUpdatedAt()));
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        product.getListPrice().ifPresentOrElse(lp -> {
            entity.setListPriceAmount(lp.money().amount());
            entity.setListPriceCurrency(lp.money().currency().getCurrencyCode());
        }, () -> {
            entity.setListPriceAmount(null);
            entity.setListPriceCurrency(null);
        });
        entity.setStatus(product.getStatus());
        entity.setUpdatedAt(product.getUpdatedAt());
        entity.setCategoryIds(product.getCategoryIds().stream()
                .map(CategoryId::value).collect(Collectors.toCollection(HashSet::new)));
        entity.setAttributes(product.getAttributes().stream()
                .map(a -> new AttributeEmbeddable(a.key(), a.value()))
                .collect(Collectors.toList()));
        entity.setMediaUrls(List.copyOf(product.getMediaUrls()));
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Product> findBySku(Sku sku) {
        return jpaRepository.findBySku(sku.value()).map(this::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return jpaRepository.existsBySku(sku.value());
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Product toDomain(ProductJpaEntity e) {
        ListPrice listPrice = null;
        if (e.getListPriceAmount() != null && e.getListPriceCurrency() != null) {
            listPrice = ListPrice.of(Money.of(e.getListPriceAmount(),
                    Currency.getInstance(e.getListPriceCurrency())));
        }
        Set<CategoryId> categoryIds = e.getCategoryIds().stream()
                .map(CategoryId::of).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        List<Attribute> attributes = e.getAttributes().stream()
                .map(a -> Attribute.of(a.getKey(), a.getValue()))
                .toList();
        return Product.reconstitute(
                ProductId.of(e.getId()),
                Sku.of(e.getSku()),
                e.getName(),
                e.getDescription(),
                listPrice,
                categoryIds,
                attributes,
                List.copyOf(e.getMediaUrls()),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
