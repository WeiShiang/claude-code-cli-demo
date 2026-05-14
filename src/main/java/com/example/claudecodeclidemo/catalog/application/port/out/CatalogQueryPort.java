package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface CatalogQueryPort {
    Money getPrice(ProductId productId);
    boolean isActive(ProductId productId);
}
