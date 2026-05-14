package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);
    Optional<Stock> findByProductId(ProductId productId);
}
