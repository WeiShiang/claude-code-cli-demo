package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

public interface CreateProductUseCase {
    ProductId createProduct(CreateProductCommand command);

    record CreateProductCommand(Sku sku, String name, String description) {}
}
