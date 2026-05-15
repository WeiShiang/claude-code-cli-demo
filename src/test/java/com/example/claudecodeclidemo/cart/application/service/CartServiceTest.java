package com.example.claudecodeclidemo.cart.application.service;

import com.example.claudecodeclidemo.cart.application.port.in.AddItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.CheckoutCommand;
import com.example.claudecodeclidemo.cart.application.port.out.CartRepository;
import com.example.claudecodeclidemo.cart.application.port.out.CatalogQueryPort;
import com.example.claudecodeclidemo.cart.application.port.out.OrderCheckoutPort;
import com.example.claudecodeclidemo.cart.domain.entity.Cart;
import com.example.claudecodeclidemo.cart.domain.event.CartCheckedOutEvent;
import com.example.claudecodeclidemo.cart.domain.exception.EmptyCartException;
import com.example.claudecodeclidemo.cart.domain.exception.ProductNotAvailableException;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CatalogQueryPort catalogQueryPort;
    @Mock OrderCheckoutPort orderCheckoutPort;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks CartService cartService;

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final UserId USER_ID = new UserId(UUID.randomUUID());
    private static final ProductId PRODUCT_A = new ProductId(UUID.randomUUID());
    private static final Quantity QTY_1 = new Quantity(1);
    private static final Money PRICE = new Money(new BigDecimal("100"), TWD);

    private Cart cartWithOneItem() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        return cart;
    }

    // ── addItem ───────────────────────────────────────────────────────────

    @Test
    void addItem_existingCart_fetchesPriceAndSaves() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(Cart.create(USER_ID)));
        when(catalogQueryPort.getPrice(PRODUCT_A)).thenReturn(PRICE);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cartService.addItem(new AddItemCommand(USER_ID, PRODUCT_A, QTY_1));

        verify(catalogQueryPort).getPrice(PRODUCT_A);
        verify(cartRepository).save(any());
    }

    @Test
    void addItem_cartNotFound_createsNewCartAndSaves() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(catalogQueryPort.getPrice(PRODUCT_A)).thenReturn(PRICE);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cartService.addItem(new AddItemCommand(USER_ID, PRODUCT_A, QTY_1));

        verify(cartRepository).save(any());
    }

    // ── checkout ──────────────────────────────────────────────────────────

    @Test
    void checkout_productNotActive_throwsProductNotAvailableException_C5() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cartWithOneItem()));
        when(catalogQueryPort.isProductActive(PRODUCT_A)).thenReturn(false);

        assertThatThrownBy(() -> cartService.checkout(new CheckoutCommand(USER_ID)))
                .isInstanceOf(ProductNotAvailableException.class);

        verify(orderCheckoutPort, never()).createOrder(any(), any());
    }

    @Test
    void checkout_emptyCart_throwsEmptyCartException_C4() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(Cart.create(USER_ID)));

        assertThatThrownBy(() -> cartService.checkout(new CheckoutCommand(USER_ID)))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void checkout_success_createsOrderAndPublishesEvent() {
        var orderId = new OrderId(UUID.randomUUID());
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cartWithOneItem()));
        when(catalogQueryPort.isProductActive(PRODUCT_A)).thenReturn(true);
        when(orderCheckoutPort.createOrder(any(), any())).thenReturn(orderId);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cartService.checkout(new CheckoutCommand(USER_ID));

        verify(orderCheckoutPort).createOrder(any(), any());
        verify(eventPublisher, atLeastOnce()).publishEvent(isA(CartCheckedOutEvent.class));
    }

    @Test
    void checkout_success_cartIsEmptyAfterCheckout() {
        var orderId = new OrderId(UUID.randomUUID());
        var cart = cartWithOneItem();
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(catalogQueryPort.isProductActive(PRODUCT_A)).thenReturn(true);
        when(orderCheckoutPort.createOrder(any(), any())).thenReturn(orderId);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cartService.checkout(new CheckoutCommand(USER_ID));

        verify(cartRepository).save(argThat(c -> c.getItems().isEmpty()));
    }
}
