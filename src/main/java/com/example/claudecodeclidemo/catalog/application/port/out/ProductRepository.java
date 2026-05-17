package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void save(Product product);
    Optional<Product> findById(ProductId id);
    Optional<Product> findBySku(Sku sku);
    boolean existsBySku(Sku sku);
    List<Product> findAll();
}
