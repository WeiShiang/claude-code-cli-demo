package com.example.claudecodeclidemo.cart.adapter.out.order;

import com.example.claudecodeclidemo.cart.application.port.out.OrderCheckoutPort;
import com.example.claudecodeclidemo.cart.domain.event.CartItemSnapshot;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCheckoutAdapter implements OrderCheckoutPort {

    @Override
    public OrderId createOrder(UserId userId, List<CartItemSnapshot> items) {
        throw new UnsupportedOperationException("Order integration not yet implemented");
    }
}
