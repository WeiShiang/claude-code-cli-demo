package com.example.claudecodeclidemo.cart.application.port.in;

import com.example.claudecodeclidemo.cart.domain.vo.OrderId;

public interface CheckoutUseCase {
    OrderId checkout(CheckoutCommand command);
}
