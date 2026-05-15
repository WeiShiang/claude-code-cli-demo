package com.example.claudecodeclidemo.cart.application.port.in;

import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;

public record RemoveItemCommand(UserId userId, ProductId productId) {}
