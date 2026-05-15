package com.example.claudecodeclidemo.cart.application.port.out;

import com.example.claudecodeclidemo.cart.domain.event.CartItemSnapshot;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;

import java.util.List;

public interface OrderCheckoutPort {
    OrderId createOrder(UserId userId, List<CartItemSnapshot> items);
}
