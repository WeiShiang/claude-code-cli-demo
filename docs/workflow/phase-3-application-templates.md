# Phase 3：Application Layer 範本

> Phase 2 僅建立 Port Interface；此 Phase 完整實作 Application Service，加上 Spring 事務邊界、事件發布與完整測試覆蓋。
> **DDD Rules**：`functional-core-imperative-shell`、`explicit-side-effects`、`error-handling`、`call-site-honesty`、`principle-of-least-astonishment`
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## Application Layer 守則

- Application Service **不含業務邏輯**（邏輯在 domain）
- Application Service **不直接操作 DB**（透過 Port Out）
- 事務邊界在 Application Service（`@Transactional`）
- 一個 Use Case = 一個公開方法
- `principle-of-least-astonishment`：副作用（事件發布、log）明確列在方法內，不轉嫁給 helper
- `call-site-honesty`：log 呼叫直接寫在 service 方法內，禁止包裝成 `logSuccess()` / `logFailure()` 等不透明 helper
- `error-handling`：對 Port Out 拋出的例外做 typed 處理，補上 aggregate id 等 context 後再 rethrow

---

## Application Service 範本

```java
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final StockReservationPort stockReservationPort;
    private final ApplicationEventPublisher eventPublisher;

    public CreateOrderService(
            OrderRepository orderRepository,
            StockReservationPort stockReservationPort,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.stockReservationPort = stockReservationPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderId createOrder(CreateOrderCommand command) {
        // 1. 保留庫存（Port Out）
        command.lines().forEach(line ->
            stockReservationPort.reserve(line.productId(), line.quantity()));

        // 2. 建立 Order Aggregate（純 domain 邏輯）
        var order = Order.create(command.userId(), command.lines());

        // 3. 持久化（Port Out）
        var saved = orderRepository.save(order);

        // 4. 發布 Domain Event
        order.getDomainEvents().forEach(eventPublisher::publishEvent);

        return saved.getId();
    }
}
```

---

## 錯誤處理範例（`error-handling` + `call-site-honesty`）

```java
@Override
public OrderId createOrder(CreateOrderCommand command) {
    try {
        command.lines().forEach(line ->
            stockReservationPort.reserve(line.productId(), line.quantity()));
    } catch (InsufficientStockException e) {
        // ✅ log 明確在呼叫處，攜帶 context
        log.warn("Stock reservation failed for user {}: productId={}, requested={}, available={}",
            command.userId(), e.getProductId(), e.getRequested(), e.getAvailable());
        throw e;  // rethrow，由 ControllerAdvice 統一處理 HTTP status
    }

    var order = Order.create(command.userId(), command.lines());
    var saved = orderRepository.save(order);
    order.getDomainEvents().forEach(eventPublisher::publishEvent);
    return saved.getId();
}
```

### 禁忌

```java
// ❌ log 藏在 helper，呼叫端看不出記了什麼
catch (InsufficientStockException e) {
    logFailure(e);  // 記了什麼？什麼 level？看不見
    throw e;
}

// ❌ silent swallow，上游無法判斷失敗
catch (Exception e) {
    return null;
}
```

---

## Application Layer 測試

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {
    // Mock Port Out，不啟動 Spring context
    @Mock OrderRepository orderRepository;
    @Mock StockReservationPort stockReservationPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks CreateOrderService service;

    @Test
    void throwsExceptionAndDoesNotSaveOrderWhenStockInsufficient() {
        doThrow(InsufficientStockException.class)
            .when(stockReservationPort).reserve(any(), anyInt());

        assertThatThrownBy(() -> service.createOrder(aCommand()))
            .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
```

---

> Git checkpoint commit prefix 見主索引附錄。
