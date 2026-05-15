package com.example.claudecodeclidemo.cart.adapter.in.web;

import com.example.claudecodeclidemo.cart.application.port.in.AddItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.CheckoutUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.RemoveItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.UpdateItemUseCase;
import com.example.claudecodeclidemo.cart.domain.exception.EmptyCartException;
import com.example.claudecodeclidemo.cart.domain.exception.ProductNotAvailableException;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CartControllerTest {

    @Autowired WebApplicationContext context;

    @MockitoBean AddItemUseCase addItemUseCase;
    @MockitoBean UpdateItemUseCase updateItemUseCase;
    @MockitoBean RemoveItemUseCase removeItemUseCase;
    @MockitoBean CheckoutUseCase checkoutUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Test
    void addItem_validRequest_returns204() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("productId", PRODUCT_ID, "quantity", 2));

        mockMvc().perform(post("/api/carts/{userId}/items", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void addItem_invalidQuantity_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("productId", PRODUCT_ID, "quantity", 0));

        mockMvc().perform(post("/api/carts/{userId}/items", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_validRequest_returns204() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("quantity", 5));

        mockMvc().perform(put("/api/carts/{userId}/items/{productId}", USER_ID, PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItem_validRequest_returns204() throws Exception {
        mockMvc().perform(delete("/api/carts/{userId}/items/{productId}", USER_ID, PRODUCT_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void checkout_success_returns200WithOrderId() throws Exception {
        var orderId = new OrderId(UUID.randomUUID());
        when(checkoutUseCase.checkout(any())).thenReturn(orderId);

        mockMvc().perform(post("/api/carts/{userId}/checkout", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.value().toString()));
    }

    @Test
    void checkout_emptyCart_returns422() throws Exception {
        when(checkoutUseCase.checkout(any())).thenThrow(new EmptyCartException());

        mockMvc().perform(post("/api/carts/{userId}/checkout", USER_ID))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void checkout_productNotAvailable_returns422() throws Exception {
        when(checkoutUseCase.checkout(any()))
                .thenThrow(new ProductNotAvailableException(new ProductId(PRODUCT_ID)));

        mockMvc().perform(post("/api/carts/{userId}/checkout", USER_ID))
                .andExpect(status().isUnprocessableEntity());
    }
}
