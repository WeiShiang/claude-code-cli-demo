# Phase 4：🔵 Refactor 檢查清單

> **DDD Rules**：`domain-specific-naming`、`function-file-size-limits`、`early-return-pattern`、`explicit-control-flow`、`command-query-separation`、`principle-of-least-astonishment`、`explicit-data-flow`、`error-handling`
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## 檢查清單

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

---

## 控制流程與 CQS 範例

### Early Return（`early-return-pattern` + `explicit-control-flow`）

```java
// ✅ Early Return：錯誤路徑先返回，主路徑無巢狀
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

### CQS（`command-query-separation`）

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

---

## Refactor Gate

```bash
./gradlew test
# 測試必須仍為全綠
```

> Git checkpoint commit prefix 見主索引附錄。
