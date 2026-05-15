package com.example.claudecodeclidemo.cart.adapter.out.persistence;

import com.example.claudecodeclidemo.cart.application.port.out.CartRepository;
import com.example.claudecodeclidemo.cart.domain.entity.Cart;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CartRepositoryAdapter implements CartRepository {

    @Override
    public Optional<Cart> findByUserId(UserId userId) {
        throw new UnsupportedOperationException("Persistence not yet implemented");
    }

    @Override
    public Cart save(Cart cart) {
        throw new UnsupportedOperationException("Persistence not yet implemented");
    }
}
