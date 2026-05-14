package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface CreateProductUseCase {
    ProductId createProduct(CreateProductCommand command);
}
