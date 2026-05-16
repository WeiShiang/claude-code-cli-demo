package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;

public interface CreateProductUseCase {

    ProductId createProduct(CreateProductCommand command);

    record CreateProductCommand(String name, Sku sku, Money price, CategoryId categoryId) {
    }
}
