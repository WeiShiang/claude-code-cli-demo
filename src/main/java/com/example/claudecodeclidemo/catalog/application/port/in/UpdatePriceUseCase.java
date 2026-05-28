package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface UpdatePriceUseCase {
    void updatePrice(UpdatePriceCommand command);

    record UpdatePriceCommand(ProductId productId, Money newPrice) {
    }
}
