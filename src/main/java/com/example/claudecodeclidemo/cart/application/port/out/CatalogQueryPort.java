package com.example.claudecodeclidemo.cart.application.port.out;

import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;

public interface CatalogQueryPort {
    Money getPrice(ProductId productId);
    boolean isProductActive(ProductId productId);
}
