# Agent Workflow — DDD + TDD 購物商城開發

> **適用工具**：Claude Code CLI  
> **Plugin**：`ddd@NeoLabHQ/context-engineering-kit`（14 條編碼規則）  
> **Skills**：`ecc:springboot-tdd`、`ecc:tdd-workflow`  
> **DDD 文件**：`docs/domain/bounded-contexts.md`、`docs/domain/ubiquitous-language.md`、`docs/domain/invariants.md`、`docs/domain/domain-events.md`

---

## 全流程總覽

```
┌─────────────────────────────────────────────────────────────────┐
│                      選擇要開發的 BC                             │
│              (Catalog / Cart / Order / Payment)                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
              ┌────────────▼────────────┐
              │   Phase 0: DDD 分析     │  ← 讀取 4 份文件
              └────────────┬────────────┘
                           │ GATE: 4 項確認全通過
              ┌────────────▼────────────┐
              │   Phase 1: 領域設計     │  ← 設計 Aggregate / Port
              └────────────┬────────────┘
                           │
              ┌────────────▼────────────┐
              │   Phase 2: 🔴 Red       │  ← 先寫測試（必須 FAIL）
              └────────────┬────────────┘
                           │ git checkpoint: RED
              ┌────────────▼────────────┐
              │   Phase 3: 🟢 Green     │  ← 實作 Domain 通過測試
              └────────────┬────────────┘
                           │ git checkpoint: GREEN
              ┌────────────▼────────────┐
              │   Phase 4: 🔵 Refactor  │  ← 重構 + ddd rules 合規
              └────────────┬────────────┘
                           │ git checkpoint: REFACTOR
              ┌────────────▼────────────┐
              │   Phase 5: Application  │  ← Use Case 實作
              └────────────┬────────────┘
              ┌────────────▼────────────┐
              │   Phase 6: Adapter      │  ← Controller + JPA
              └────────────┬────────────┘
              ┌────────────▼────────────┐
              │   Phase 7: 測試驗收     │  ← 全套測試 + Coverage
              └────────────┬────────────┘
              ┌────────────▼────────────┐
              │   Phase 8: 開發報告     │  ← 產出 Report
              └─────────────────────────┘
```

---

## Phase 0：DDD 分析

> **目標**：從 4 份 DDD 文件萃取本次開發 BC 的所有設計約束。  
> **Plugin 啟用**：`ddd@NeoLabHQ/context-engineering-kit`（規則自動附加到 context）

### 0.1 讀取文件

```
讀取順序：
1. docs/domain/bounded-contexts.md   → 確認 BC 邊界、Aggregate、Port 清單
2. docs/domain/ubiquitous-language.md → 確認術語對照（命名必須一致）
3. docs/domain/invariants.md          → 列出本 BC 所有不變量
4. docs/domain/domain-events.md       → 列出本 BC 發布/訂閱的事件
```

### 0.2 四項確認 Gate（全部通過才進 Phase 1）

| # | 確認項目 | 通過條件 |
|---|---|---|
| G0-1 | **Bounded Context** | 清楚知道本 BC 的 Aggregate Root、Port In/Out |
| G0-2 | **Ubiquitous Language** | 所有類別/方法命名已對照詞彙表，無使用禁用詞 |
| G0-3 | **Invariant** | 本 BC 的所有不變量已列成清單，對應 Exception 名稱確認 |
| G0-4 | **Domain Event** | 發布與訂閱的事件清單確認，schema 欄位確認 |

### 0.3 輸出物

```markdown
## [BC 名稱] 分析摘要

### Aggregate
- Aggregate Root: XXX
- Entities: [...]
- Value Objects: [...]

### Invariants（本 BC）
- [BC]-1: [規則描述] → 違反拋 [ExceptionName]
- ...

### Domain Events
- 發布: [EventName], [EventName]
- 訂閱: [EventName]

### Port 清單
- In: [UseCaseName], ...
- Out: [RepositoryName], ...
```

---

## Phase 1：領域設計

> **目標**：決定程式碼結構，不寫任何實作。  
> **DDD Rule 參考**：`clean-architecture-ddd.md`、`separation-of-concerns.md`、`domain-specific-naming.md`

### 1.1 Package 結構規劃

```
com.example.claudecodeclidemo.<bc>/
├── domain/
│   ├── entity/          ← Aggregate Root, Entity
│   ├── vo/              ← Value Object（不可變）
│   ├── event/           ← Domain Event（過去式命名）
│   └── exception/       ← Domain Exception（對應 Invariant）
├── application/
│   └── port/
│       ├── in/          ← Use Case Interface
│       └── out/         ← Repository / External Port Interface
└── adapter/
    ├── in/
    │   └── web/         ← Controller + DTO
    └── out/
        └── persistence/ ← JPA Entity + Repository Impl
```

### 1.2 設計檢查清單

- [ ] Domain 層無任何 `import org.springframework.*`
- [ ] Domain 層無任何 `import javax.persistence.*` / `jakarta.persistence.*`
- [ ] Value Object 實作 `equals()`、`hashCode()`、不可變（`final` fields）
- [ ] Aggregate Root 是唯一修改內部狀態的入口
- [ ] Port Interface 定義在 `application/port/` 下，命名以 `UseCase` 或 `Port` 結尾
- [ ] Domain Event 為不可變 record / class，欄位含 `occurredAt: Instant`

### 1.3 DDD 規則對照

| 設計決策 | 對應 DDD Rule |
|---|---|
| Domain entity 不 import Spring/JPA | `clean-architecture-ddd` |
| 每個 class 單一職責 | `separation-of-concerns` |
| 命名使用 Ubiquitous Language | `domain-specific-naming` |
| Use Case 方法只做一件事（查詢或命令） | `command-query-separation` |
| 副作用（event 發布、外部呼叫）明確在方法中標示 | `explicit-side-effects` |

---

## Phase 2：🔴 Red（寫測試，必須 FAIL）

> **Skill**：`ecc:springboot-tdd` + `ecc:tdd-workflow`  
> **規則**：未確認 RED 前，不得修改任何 production code

### 2.1 測試撰寫優先順序

```
1. Value Object 單元測試      → 驗證建構規則、equals、不變量
2. Domain Entity 單元測試     → 驗證狀態轉換、不變量、Domain Event 發布
3. Use Case 單元測試          → Mock Port Out，驗證業務邏輯
4. Invariant 負向測試         → 每條 Invariant 至少一個 FAIL 測試案例
```

### 2.2 測試範本（Spring Boot / JUnit 5）

#### Value Object 測試
```java
class MoneyTest {
    @Test
    void 金額不可為負數() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"), Currency.TWD))
            .isInstanceOf(InvalidPriceException.class);
    }

    @Test
    void 相同金額與幣別的Money相等() {
        var a = new Money(new BigDecimal("100"), Currency.TWD);
        var b = new Money(new BigDecimal("100"), Currency.TWD);
        assertThat(a).isEqualTo(b);
    }
}
```

#### Domain Entity 不變量測試
```java
@ExtendWith(MockitoExtension.class)
class OrderTest {
    @Test
    void 無訂單明細時不可建立訂單() {
        assertThatThrownBy(() -> Order.create(userId, List.of()))
            .isInstanceOf(EmptyOrderException.class);
    }

    @Test
    void CREATED狀態可取消() {
        var order = OrderTestBuilder.aCreatedOrder().build();
        order.cancel(CancellationReason.USER_REQUEST);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void PAID狀態不可由使用者取消() {
        var order = OrderTestBuilder.aPaidOrder().build();
        assertThatThrownBy(() -> order.cancel(CancellationReason.USER_REQUEST))
            .isInstanceOf(OrderCancellationNotAllowedException.class);
    }

    @Test
    void 建立訂單時發布OrderCreatedEvent() {
        var order = Order.create(userId, List.of(aLine()));
        assertThat(order.getDomainEvents())
            .hasSize(1)
            .first().isInstanceOf(OrderCreatedEvent.class);
    }
}
```

#### Use Case 測試（Mock Port）
```java
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {
    @Mock OrderRepository orderRepository;
    @Mock StockReservationPort stockReservationPort;
    @InjectMocks CreateOrderService createOrderService;

    @Test
    void 成功建立訂單並保留庫存() {
        // Arrange
        var command = new CreateOrderCommand(userId, List.of(anItem()));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var order = createOrderService.createOrder(command);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(stockReservationPort).reserve(any());
        verify(orderRepository).save(any());
    }
}
```

### 2.3 確認 RED Gate

```bash
./gradlew test
# 預期：測試全部 FAIL（紅燈）
# 確認失敗原因是「實作不存在」，而非 syntax error
```

**Git Checkpoint — RED**
```bash
git add src/test/
git commit -m "test: add red tests for [BC名稱] - [功能描述]"
```

> ⚠️ 未執行測試確認 FAIL 前，禁止進入 Phase 3

---

## Phase 3：🟢 Green（最小實作通過測試）

> **DDD Rule**：`functional-core-imperative-shell`（純 domain 邏輯，副作用在外層）

### 3.1 實作順序

```
1. Value Objects（不可變，含驗證）
2. Domain Exceptions（對應 Invariant 清單）
3. Domain Entities / Aggregate Root（含狀態機、不變量守衛）
4. Domain Events（record / immutable class）
5. Port Interfaces（application/port/in, application/port/out）
6. Application Service（Use Case 實作，注入 Port Out）
```

### 3.2 Domain 層純度守則（ddd plugin rules）

```java
// ✅ CORRECT: Domain Entity 無 framework import
package com.example.claudecodeclidemo.order.domain.entity;

// 只允許 java.* import
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    // ...
}
```

```java
// ❌ WRONG: Domain 層混入 JPA annotation
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity  // ← 絕對禁止
public class Order { }
```

### 3.3 Value Object 範本

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException(amount);
        }
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException();
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    @Override
    public boolean equals(Object o) { /* ... */ }

    @Override
    public int hashCode() { /* ... */ }
}
```

### 3.4 確認 GREEN Gate

```bash
./gradlew test
# 預期：所有測試 PASS（綠燈）
```

**Git Checkpoint — GREEN**
```bash
git add src/main/java/ src/test/java/
git commit -m "feat: implement [BC名稱] domain layer - [功能描述]"
```

---

## Phase 4：🔵 Refactor（重構，測試保持綠燈）

> **DDD Rule**：`domain-specific-naming`、`function-file-size-limits`、`early-return-pattern`、`explicit-control-flow`

### 4.1 重構檢查清單

**命名（`domain-specific-naming` rule）**
- [ ] 無 `Utils`、`Helper`、`Manager`、`Common` 類別名稱
- [ ] 方法名稱使用 Ubiquitous Language 詞彙表中的動詞
- [ ] Exception 名稱對應 `invariants.md` 清單

**函式設計（`function-file-size-limits` rule）**
- [ ] 每個方法 ≤ 20 行
- [ ] 每個 class ≤ 200 行
- [ ] 複雜條件提取為 private 方法（具名）

**控制流程（`explicit-control-flow` + `early-return-pattern`）**
```java
// ✅ Early Return（明確的錯誤路徑）
public void reserve(int quantity) {
    if (quantity <= 0) throw new InvalidQuantityException(quantity);
    if (availableQuantity() < quantity) throw new InsufficientStockException();
    this.reserved += quantity;
}

// ❌ 巢狀 if（難以追蹤控制流程）
public void reserve(int quantity) {
    if (quantity > 0) {
        if (availableQuantity() >= quantity) {
            this.reserved += quantity;
        } else { throw new InsufficientStockException(); }
    } else { throw new InvalidQuantityException(quantity); }
}
```

**CQS（`command-query-separation` rule）**
```java
// ✅ 查詢（有回傳值，無副作用）
public int availableQuantity() {
    return this.quantity - this.reserved;
}

// ✅ 命令（有副作用，回傳 void 或 this）
public void reserve(int quantity) { ... }

// ❌ 違反 CQS（既查詢又修改）
public int reserveAndReturnRemaining(int quantity) { ... }
```

### 4.2 重構後驗證

```bash
./gradlew test
# 測試必須仍為全綠
```

**Git Checkpoint — REFACTOR**
```bash
git commit -m "refactor: clean up [BC名稱] domain layer"
```

---

## Phase 5：Application Layer 實作

> **目標**：Use Case 串接 Domain 與 Port Out，協調跨 Aggregate 操作。

### 5.1 Application Service 範本

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

### 5.2 Application Layer 守則

- Application Service **不包含業務邏輯**（邏輯在 domain）
- Application Service **不直接操作 DB**（透過 Port Out）
- 事務邊界在 Application Service（`@Transactional`）
- 一個 Use Case = 一個公開方法

### 5.3 Application Layer 測試

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {
    // Mock Port Out，不啟動 Spring context
    @Mock OrderRepository orderRepository;
    @Mock StockReservationPort stockReservationPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks CreateOrderService service;

    @Test
    void 庫存不足時拋出例外並不建立訂單() {
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

## Phase 6：Adapter Layer 實作

### 6.1 Adapter In（Web）

**Controller 範本**
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

**Controller 單元測試（MockMvc）**
```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean CreateOrderUseCase createOrderUseCase;

    @Test
    void 建立訂單回傳201() throws Exception {
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

### 6.2 Adapter Out（Persistence）

**JPA Entity（與 Domain Entity 完全分離）**
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

**Repository Adapter**
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

**Integration Test（Testcontainers）**
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void 建立訂單完整流程() throws Exception {
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

## Phase 7：測試驗收

### 7.1 執行全套測試

```bash
# 單元 + 整合測試 + JaCoCo 報告
./gradlew test jacocoTestReport

# 開啟覆蓋率報告
open build/reports/jacoco/test/html/index.html
```

### 7.2 Coverage Gate（JaCoCo）

| 層次 | 最低門檻 |
|---|---|
| Domain Layer | **≥ 90%** line coverage |
| Application Layer | **≥ 80%** line coverage |
| Adapter Layer | **≥ 70%** line coverage |
| 整體 | **≥ 60%** line coverage（專案設定） |

### 7.3 DDD 合規檢查

```bash
# 確認 Domain 層無 Spring/JPA import
grep -r "import org.springframework" src/main/java/com/example/claudecodeclidemo/*/domain/
grep -r "import jakarta.persistence" src/main/java/com/example/claudecodeclidemo/*/domain/
# 預期：無任何輸出
```

### 7.4 驗收 Gate

| # | 檢查項目 | 通過條件 |
|---|---|---|
| V-1 | 所有測試通過 | `BUILD SUCCESS`，0 failures |
| V-2 | Coverage 達標 | JaCoCo 報告 line coverage ≥ 60% |
| V-3 | Domain 純度 | Domain 層無 Spring / JPA import |
| V-4 | 命名合規 | 類別/方法名稱符合 `ubiquitous-language.md` |
| V-5 | Invariant 測試 | 每條 Invariant 有對應的負向測試 |
| V-6 | Event 測試 | 每個 Domain Event 有驗證發布的測試 |

---

## Phase 8：開發報告

> 每個 BC 開發完成後，產出以下報告存至 `docs/dev-reports/<bc-name>-report.md`

### 報告範本

```markdown
# [BC 名稱] 開發報告

**日期**：YYYY-MM-DD  
**開發者**：Claude Code CLI  
**Branch**：feature/<bc-name>

---

## 實作摘要

### Aggregate 清單
| Class | 類型 | 說明 |
|---|---|---|
| `Order` | Aggregate Root | ... |
| `OrderLine` | Entity | ... |
| `Money` | Value Object | ... |

### Use Case 清單
| Interface | 實作 | 說明 |
|---|---|---|
| `CreateOrderUseCase` | `CreateOrderService` | ... |

### Domain Event 清單
| Event | 觸發時機 | 消費者 |
|---|---|---|
| `OrderCreatedEvent` | 訂單建立 | Payment BC |

---

## TDD 循環記錄

| 循環 | 測試案例 | RED commit | GREEN commit | REFACTOR commit |
|---|---|---|---|---|
| 1 | Order 建立不變量 | abc1234 | def5678 | ghi9012 |
| 2 | 訂單狀態轉換 | ... | ... | ... |

---

## 覆蓋率報告

| 層次 | Line Coverage | Branch Coverage |
|---|---|---|
| domain | XX% | XX% |
| application | XX% | XX% |
| adapter | XX% | XX% |
| **整體** | **XX%** | **XX%** |

---

## DDD Rules 合規結果

| Rule | 狀態 | 備註 |
|---|---|---|
| clean-architecture-ddd | ✅ PASS | Domain 層無 framework import |
| domain-specific-naming | ✅ PASS | 所有命名符合 UL |
| command-query-separation | ✅ PASS | |
| separation-of-concerns | ✅ PASS | |
| explicit-side-effects | ✅ PASS | |

---

## 已知限制 / 後續待辦

- [ ] ...
```

---

## BC 開發順序建議

依賴關係由淺入深：

```
1. Catalog   ← 無依賴，最先開發
2. Order     ← 依賴 Catalog（StockReservationPort）
3. Payment   ← 依賴 Order（PaymentPort）
4. Cart      ← 依賴 Catalog（CatalogQueryPort）+ Order（OrderCheckoutPort）
```

---

## 快速參考：Skill 呼叫時機

| 時機 | Skill / Plugin |
|---|---|
| 進入任何 BC 開發 | `ddd` plugin 自動生效（規則附加到 context） |
| Phase 2 ~ 4（TDD 循環） | `/ecc:springboot-tdd` |
| 全域 TDD 規範確認 | `/ecc:tdd-workflow` |
| 架構設計決策 | `/ecc:hexagonal-architecture` |
| 程式碼 review | `/ecc:code-review` |
| 安全性檢查 | `/ecc:security-review` |
| Coverage 不足修補 | `/ecc:test-coverage` |

---

## 附錄：Git Commit 慣例

| 階段 | Prefix | 範例 |
|---|---|---|
| Red | `test:` | `test: add red tests for Order invariants` |
| Green | `feat:` | `feat: implement Order domain layer` |
| Refactor | `refactor:` | `refactor: clean up Order domain layer` |
| Application | `feat:` | `feat: implement CreateOrderService` |
| Adapter | `feat:` | `feat: implement Order REST adapter and JPA adapter` |
| Fix | `fix:` | `fix: handle empty cart in checkout` |
