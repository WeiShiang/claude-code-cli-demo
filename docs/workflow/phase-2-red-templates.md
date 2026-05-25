# Phase 2：🔴 Red 測試範本

> **Skill**：`ecc:tdd-workflow`
> **規則**：未確認 RED 前，不得修改任何 production code
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## 測試撰寫順序

| # | 測試類型 | 重點 |
|---|---|---|
| 1 | Value Object 單元測試 | 建構規則、equals、不變量 |
| 2 | Domain Entity 不變量負向測試 | 每條 Invariant 至少一個 FAIL 案例 |
| 3 | Domain Entity 狀態轉換測試 | 合法狀態轉換路徑 |
| 4 | Domain Event 發布測試 | 操作後事件被加入 `domainEvents` |
| 5 | Use Case 單元測試 | Mock Port Out，驗證業務流程協調 |

---

## 範本（Spring Boot / JUnit 5）

### Value Object 測試

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

### Domain Entity（不變量 + 狀態轉換 + Event）

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

### Use Case（Mock Port）

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {
    @Mock OrderRepository orderRepository;
    @Mock StockReservationPort stockReservationPort;
    @InjectMocks CreateOrderService createOrderService;

    @Test
    void successfullyCreatesOrderAndReservesStock() {
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

---

## RED Gate

```bash
./gradlew test
# 預期：測試全部 FAIL（紅燈）
# 確認失敗原因是「實作不存在」，而非 syntax error
```

> ⚠️ 未執行測試確認 FAIL 前，禁止進入 Phase 3。
> Git checkpoint commit prefix 見主索引附錄。
