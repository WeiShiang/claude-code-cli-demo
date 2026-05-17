package com.example.claudecodeclidemo;

import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.DomainEventPublisher;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ClaudeCodeCliDemoApplicationTests {

	@MockitoBean ProductRepository productRepository;
	@MockitoBean CategoryRepository categoryRepository;
	@MockitoBean DomainEventPublisher domainEventPublisher;

	@Test
	void contextLoads() {
	}

}
