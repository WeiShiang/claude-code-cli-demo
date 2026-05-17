package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QueryProductService implements QueryProductUseCase {

    private final ProductRepository productRepository;

    public QueryProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductView findById(ProductId productId) {
        return productRepository.findById(productId)
                .map(this::toView)
                .orElseThrow(() -> new ProductNotFoundException(
                        "product not found: " + productId.value()));
    }

    @Override
    public ProductView findBySku(Sku sku) {
        return productRepository.findBySku(sku)
                .map(this::toView)
                .orElseThrow(() -> new ProductNotFoundException(
                        "product not found: " + sku.value()));
    }

    @Override
    public List<ProductView> findAll() {
        return productRepository.findAll().stream().map(this::toView).toList();
    }

    private ProductView toView(Product p) {
        return new ProductView(
                p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getListPrice(), p.getCategoryIds(), p.getAttributes(), p.getMediaUrls(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
