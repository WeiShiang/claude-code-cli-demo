package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface UpdateStockUseCase {

    void reserve(ProductId productId, int amount);
    void release(ProductId productId, int amount);
    void deduct(ProductId productId, int amount);
}
