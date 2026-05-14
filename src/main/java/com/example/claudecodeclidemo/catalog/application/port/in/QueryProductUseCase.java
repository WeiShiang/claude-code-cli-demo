package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface QueryProductUseCase {
    Product findById(ProductId id);
}
