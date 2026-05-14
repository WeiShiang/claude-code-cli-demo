package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface StockReservationPort {
    void reserve(ProductId productId, int quantity);
    void release(ProductId productId, int quantity);
    void deduct(ProductId productId, int quantity);
}
