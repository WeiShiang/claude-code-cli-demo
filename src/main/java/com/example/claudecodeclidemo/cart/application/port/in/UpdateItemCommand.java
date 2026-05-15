package com.example.claudecodeclidemo.cart.application.port.in;

import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;

public record UpdateItemCommand(UserId userId, ProductId productId, Quantity quantity) {}
