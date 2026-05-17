package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface RemoveCategoryUseCase {
    void removeCategory(ProductId productId, CategoryId categoryId);
}
