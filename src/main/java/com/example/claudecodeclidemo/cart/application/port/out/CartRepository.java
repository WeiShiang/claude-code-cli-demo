package com.example.claudecodeclidemo.cart.application.port.out;

import com.example.claudecodeclidemo.cart.domain.entity.Cart;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findByUserId(UserId userId);
    Cart save(Cart cart);
}
