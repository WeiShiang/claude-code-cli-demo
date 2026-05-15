package com.example.claudecodeclidemo.cart.adapter.in.web;

import java.util.UUID;

record AddItemRequest(UUID productId, int quantity) {}
