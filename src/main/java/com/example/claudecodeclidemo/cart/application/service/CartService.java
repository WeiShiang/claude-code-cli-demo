package com.example.claudecodeclidemo.cart.application.service;

import com.example.claudecodeclidemo.cart.application.port.in.AddItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.AddItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.CheckoutCommand;
import com.example.claudecodeclidemo.cart.application.port.in.CheckoutUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.RemoveItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.RemoveItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.UpdateItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.UpdateItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.out.CartRepository;
import com.example.claudecodeclidemo.cart.application.port.out.CatalogQueryPort;
import com.example.claudecodeclidemo.cart.application.port.out.OrderCheckoutPort;
import com.example.claudecodeclidemo.cart.domain.entity.Cart;
import com.example.claudecodeclidemo.cart.domain.event.CartItemSnapshot;
import com.example.claudecodeclidemo.cart.domain.exception.CartNotFoundException;
import com.example.claudecodeclidemo.cart.domain.exception.EmptyCartException;
import com.example.claudecodeclidemo.cart.domain.exception.ProductNotAvailableException;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CartService implements AddItemUseCase, UpdateItemUseCase, RemoveItemUseCase, CheckoutUseCase {

    private final CartRepository cartRepository;
    private final CatalogQueryPort catalogQueryPort;
    private final OrderCheckoutPort orderCheckoutPort;
    private final ApplicationEventPublisher eventPublisher;

    public CartService(CartRepository cartRepository,
                       CatalogQueryPort catalogQueryPort,
                       OrderCheckoutPort orderCheckoutPort,
                       ApplicationEventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.catalogQueryPort = catalogQueryPort;
        this.orderCheckoutPort = orderCheckoutPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void addItem(AddItemCommand command) {
        var cart = cartRepository.findByUserId(command.userId())
                .orElseGet(() -> Cart.create(command.userId()));
        var price = catalogQueryPort.getPrice(command.productId());
        cart.addItem(command.productId(), command.quantity(), price);
        cartRepository.save(cart);
    }

    @Override
    public void updateItem(UpdateItemCommand command) {
        var cart = loadCart(command.userId());
        cart.updateItemQuantity(command.productId(), command.quantity());
        cartRepository.save(cart);
    }

    @Override
    public void removeItem(RemoveItemCommand command) {
        var cart = loadCart(command.userId());
        cart.removeItem(command.productId());
        cartRepository.save(cart);
    }

    @Override
    public OrderId checkout(CheckoutCommand command) {
        var cart = loadCart(command.userId());
        if (cart.getItems().isEmpty()) throw new EmptyCartException();
        validateAllProductsActive(cart);
        var snapshots = buildSnapshots(cart);
        var orderId = orderCheckoutPort.createOrder(command.userId(), snapshots);
        cart.checkout(orderId);
        cartRepository.save(cart);
        cart.getDomainEvents().forEach(eventPublisher::publishEvent);
        cart.clearDomainEvents();
        return orderId;
    }

    private Cart loadCart(UserId userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    private void validateAllProductsActive(Cart cart) {
        cart.getItems().forEach(item -> {
            if (!catalogQueryPort.isProductActive(item.getProductId())) {
                throw new ProductNotAvailableException(item.getProductId());
            }
        });
    }

    private List<CartItemSnapshot> buildSnapshots(Cart cart) {
        return cart.getItems().stream()
                .map(i -> new CartItemSnapshot(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();
    }
}
