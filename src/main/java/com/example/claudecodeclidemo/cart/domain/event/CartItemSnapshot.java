package com.example.claudecodeclidemo.cart.domain.event;

import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;

public record CartItemSnapshot(ProductId productId, Quantity quantity, Money unitPrice) {}
