package com.example.claudecodeclidemo;

import com.example.claudecodeclidemo.catalog.application.port.out.CategoryExistsPort;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ClaudeCodeCliDemoApplicationTests {

	@MockitoBean ProductRepository productRepository;
	@MockitoBean StockRepository stockRepository;
	@MockitoBean CategoryExistsPort categoryExistsPort;

	@Test
	void contextLoads() {
	}

}
