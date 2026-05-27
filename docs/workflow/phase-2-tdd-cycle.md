# Phase 2：🔴🟢🔵 TDD 循環

> **Skill**：`ecc:tdd-workflow`
> **DDD Rules**：`functional-core-imperative-shell`、`explicit-data-flow`、`error-handling`、`domain-specific-naming`、`function-file-size-limits`、`early-return-pattern`、`explicit-control-flow`、`command-query-separation`、`principle-of-least-astonishment`
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## 執行模式

每個 Invariant / Use Case 走一個完整微循環，完成所有項目後才進 Gate：

```
for each invariant / use case:
  2a 🔴 Red     → 寫測試（確認 FAIL）
  2b 🟢 Green   → 最小實作（確認 PASS）
  2c 🔵 Refactor → DDD rules 合規 + 測試仍綠
```

---

## 2a 🔴 Red：測試撰寫

> 規則：未確認 RED 前，不得修改任何 production code

### 測試撰寫順序

| # | 測試類型 | 重點 |
|---|---|---|
| 1 | Value Object 單元測試 | 建構規則、equals、不變量 |
| 2 | Domain Entity 不變量負向測試 | 每條 Invariant 至少一個 FAIL 案例 |
| 3 | Domain Entity 狀態轉換測試 | 合法狀態轉換路徑 |
| 4 | Domain Event 發布測試 | 操作後事件被加入 `domainEvents` |
| 5 | Use Case 單元測試 | Mock Port Out，驗證業務流程協調 |

### 範本（Spring Boot / JUnit 5）

#### Value Object 測試

```java
class MoneyTest {
    @Test
    void amountMustNotBeNegative() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"), Currency.TWD))
            .isInstanceOf(InvalidPriceException.class);
    }

    @Test
    void moneyWithSameAmountAndCurrencyAreEqual() {
        var a = new Money(new BigDecimal("100"), Currency.TWD);
        var b = new Money(new BigDecimal("100"), Currency.TWD);
        assertThat(a).isEqualTo(b);
    }
}
```

#### Domain Entity（不變量 + 狀態轉換 + Event）

```java
class OrderTest {
    @Test
    void cannotCreateOrderWithoutLines() {
        assertThatThrownBy(() -> Order.create(userId, List.of()))
            .isInstanceOf(EmptyOrderException.class);
    }

    @Test
    void createdOrderCanBeCancelled() {
        var order = OrderTestBuilder.aCreatedOrder().build();
        order.cancel(CancellationReason.USER_REQUEST);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void paidOrderCannotBeCancelledByUser() {
        var order = OrderTestBuilder.aPaidOrder().build();
        assertThatThrownBy(() -> order.cancel(CancellationReason.USER_REQUEST))
            .isInstanceOf(OrderCancellationNotAllowedException.class);
    }

    @Test
    void publishesOrderCreatedEventOnCreate() {
        var order = Order.create(userId, List.of(aLine()));
        assertThat(order.getDomainEvents())
            .hasSize(1)
            .first().isInstanceOf(OrderCreatedEvent.class);
    }
}
```

#### Use Case（Mock Port）

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {
    @Mock OrderRepository orderRepository;
    @Mock StockReservationPort stockReservationPort;
    @InjectMocks CreateOrderService createOrderService;

    @Test
    void successfullyCreatesOrderAndReservesStock() {
        var command = new CreateOrderCommand(userId, List.of(anItem()));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var order = createOrderService.createOrder(command);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(stockReservationPort).reserve(any());
        verify(orderRepository).save(any());
    }
}
```

### Red 確認

```bash
./gradlew test
# 預期：測試全部 FAIL（紅燈）
# 確認失敗原因是「實作不存在」，而非 syntax error
```

---

## 2b 🟢 Green：Domain 實作

> **範圍**：Domain 層 + Port Interface。Application Service 完整實作保留至 Phase 3。

### 實作順序

1. Value Objects（不可變，含驗證）
2. Domain Exceptions（對應 Invariant 清單）
3. Domain Entities / Aggregate Root（含狀態機、不變量守衛）
4. Domain Events（immutable record / class，欄位含 `occurredAt: Instant`）
5. Port Interfaces（`application/port/{in,out}`）

### Domain 層純度

```java
// ✅ 只允許 java.* import
package com.example.claudecodeclidemo.order.domain.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order { /* ... */ }
```

**禁止**：`@Entity`、`@Table`、`@Service`、`@Component` 等 framework annotation 出現在 domain 層。

### Domain Exception 設計（`error-handling`）

不變量違反時拋出 typed Domain Exception，禁用 `RuntimeException`、`IllegalStateException` 等泛型。名稱必須對應 `invariants.md`。

```java
public class InsufficientStockException extends RuntimeException {
    private final ProductId productId;
    private final int requested;
    private final int available;

    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Insufficient stock for product %s: requested=%d, available=%d"
            .formatted(productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public ProductId getProductId() { return productId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
```

### Value Object 範本（`explicit-data-flow`）

VO 不可變；「修改」必須回傳新實例，禁止 setter 或內部 mutation。

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null)
            throw new IllegalArgumentException("amount and currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidPriceException(amount);
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new CurrencyMismatchException(this.currency, other.currency);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
```

### Green 確認

```bash
./gradlew test
# 預期：所有測試 PASS（綠燈）
```

---

## 2c 🔵 Refactor：重構與合規

### 命名（`domain-specific-naming` + `principle-of-least-astonishment`）

- [ ] 無 `Utils` / `Helper` / `Manager` / `Common` 類別名稱
- [ ] 方法名稱使用 Ubiquitous Language 動詞
- [ ] Exception 名稱對應 `invariants.md`
- [ ] 方法只做名稱承諾的事：`getX()` 不修改狀態、`validateX()` 不發送事件
- [ ] 隱式副作用（log、analytics、cache）不藏在 getter / query 內

### 函式設計（`function-file-size-limits`）

- [ ] 每個方法 ≤ 20 行
- [ ] 每個 class ≤ 200 行
- [ ] 複雜條件提取為具名 private 方法

### 資料流（`explicit-data-flow`）

- [ ] VO 操作回傳新實例（如 `Money.add` 回傳新 Money）
- [ ] 不透過參數 mutation 回傳結果
- [ ] 局部變數優先使用 `final`

### 錯誤處理（`error-handling`）

- [ ] 不使用泛型 `catch (Exception e)`，必須 typed catch
- [ ] 每個 catch 區塊在 rethrow 前以 log 記錄含 context（aggregate id、operation name）
- [ ] Domain Exception 攜帶足夠 context 欄位，呼叫端可區分原因
- [ ] 禁止 silent swallow（`catch` 後 `return null` / 不做任何事）

### 控制流程範例

#### Early Return（`early-return-pattern` + `explicit-control-flow`）

```java
// ✅
public void reserve(int quantity) {
    if (quantity <= 0) throw new InvalidQuantityException(quantity);
    if (availableQuantity() < quantity) throw new InsufficientStockException();
    this.reserved += quantity;
}

// ❌ 巢狀 if
public void reserve(int quantity) {
    if (quantity > 0) {
        if (availableQuantity() >= quantity) {
            this.reserved += quantity;
        } else { throw new InsufficientStockException(); }
    } else { throw new InvalidQuantityException(quantity); }
}
```

#### CQS（`command-query-separation`）

```java
// ✅ 查詢（有回傳值，無副作用）
public int availableQuantity() {
    return this.quantity - this.reserved;
}

// ✅ 命令（有副作用，回傳 void 或 this）
public void reserve(int quantity) { /* ... */ }

// ❌ 違反 CQS（既查詢又修改）
public int reserveAndReturnRemaining(int quantity) { /* ... */ }
```

### Refactor 確認

```bash
./gradlew test
# 測試必須仍為全綠
```

---

## Gate G2（全通過才進 Phase 3）

| # | 項目 | 通過條件 |
|---|---|---|
| G2-1 | 測試覆蓋 | 每個 Invariant 都有對應測試，且全部綠燈 |
| G2-2 | Domain 純度 | Domain 層無 Spring / JPA import |
| G2-3 | DDD 合規 | Refactor 清單全部勾選 |
| G2-4 | CQS | 所有方法符合 Command 或 Query 其中一種 |

```bash
./gradlew test
# 全綠 → git commit（prefix: refactor:）→ 等待使用者確認
```
