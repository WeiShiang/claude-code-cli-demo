# Phase 4：Adapter Layer 範本

> **DDD Rules**：`library-first-approach`、`clean-architecture-ddd`、`separation-of-concerns`
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## Library-First 對照表（`library-first-approach`）

Adapter 層充滿橫切關注（驗證、序列化、重試、mapping）。寫任何 utility 前，先確認 Spring 生態與 java.* 是否已提供：

| 需求 | ✅ 使用 | ❌ 不要手刻 |
|---|---|---|
| Bean validation | `jakarta.validation`（`@NotNull`、`@Min`） | controller 內 if-check |
| DTO ↔ Domain 轉換 | MapStruct、手寫 mapper（單純情境） | reflection-based 自製 mapper |
| 重試 / circuit breaker | Spring Retry、Resilience4j | 自寫 `for + sleep` 重試 |
| JSON 序列化 | Jackson + `@JsonProperty` | 自行拼接 JSON 字串 |
| Optimistic locking | JPA `@Version` | 自行讀寫 version 欄位 |
| 全域例外處理 | `@RestControllerAdvice` | controller 內 try-catch |

---

## Adapter In（Web）

### Controller 範本

```java
@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderIdResponse createOrder(@RequestBody @Valid CreateOrderRequest request) {
        var command = OrderRequestMapper.toCommand(request);
        var orderId = createOrderUseCase.createOrder(command);
        return new OrderIdResponse(orderId.value());
    }
}
```

### Controller 單元測試（WebMvcTest）

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean CreateOrderUseCase createOrderUseCase;

    @Test
    void createOrderReturns201() throws Exception {
        when(createOrderUseCase.createOrder(any()))
            .thenReturn(new OrderId(UUID.randomUUID()));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"userId": "user-1", "lines": [
                      {"productId": "prod-1", "quantity": 2}
                    ]}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists());
    }
}
```

---

## Adapter Out（Persistence）

### JPA Entity（與 Domain Entity 完全分離）

```java
@Entity
@Table(name = "orders")
class OrderJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderLineJpaEntity> lines = new ArrayList<>();
}
```

### Repository Adapter

```java
@Component
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        var entity = mapper.toJpa(order);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }
}
```

### Integration Test（Testcontainers）

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void createOrderFullFlow() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"userId": "user-1", "lines": [
                      {"productId": "prod-1", "quantity": 1}
                    ]}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists());
    }
}
```

---

> Git checkpoint commit prefix 見主索引附錄。
