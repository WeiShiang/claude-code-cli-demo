package com.example.claudecodeclidemo.cart.adapter.out.catalog;

import com.example.claudecodeclidemo.cart.application.port.out.CatalogQueryPort;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import org.springframework.stereotype.Component;

@Component
public class CatalogQueryAdapter implements CatalogQueryPort {

    @Override
    public Money getPrice(ProductId productId) {
        throw new UnsupportedOperationException("Catalog integration not yet implemented");
    }

    @Override
    public boolean isProductActive(ProductId productId) {
        throw new UnsupportedOperationException("Catalog integration not yet implemented");
    }
}
