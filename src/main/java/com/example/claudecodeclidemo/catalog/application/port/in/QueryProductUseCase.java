package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

import java.util.Optional;

public interface QueryProductUseCase {

    Optional<Product> findById(ProductId id);
}
