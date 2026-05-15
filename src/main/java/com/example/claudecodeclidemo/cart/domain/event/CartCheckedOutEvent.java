package com.example.claudecodeclidemo.cart.domain.event;

import com.example.claudecodeclidemo.cart.domain.vo.CartId;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;

import java.time.Instant;
import java.util.List;

public record CartCheckedOutEvent(
        CartId cartId,
        UserId userId,
        OrderId orderId,
        List<CartItemSnapshot> items,
        Instant occurredAt
) {}
