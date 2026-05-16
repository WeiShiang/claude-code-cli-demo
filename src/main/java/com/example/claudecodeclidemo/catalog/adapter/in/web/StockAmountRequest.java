package com.example.claudecodeclidemo.catalog.adapter.in.web;

import jakarta.validation.constraints.Min;

record StockAmountRequest(@Min(1) int amount) {}
