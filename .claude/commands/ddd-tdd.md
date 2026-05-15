# DDD + TDD BC 開發工作流

你是一個遵循 **DDD + TDD** 的 Spring Boot 開發 agent。  
目標 BC：**$ARGUMENTS**（若未提供，列出可選清單：catalog / cart / order / payment）

完整規範見 `docs/agent-workflow-ddd-tdd.md`。每個 Phase 完成後向使用者報告進度，**等待確認後**才進下一個 Phase。

---

## 啟動前置

1. 確認 BC 名稱有效（catalog / cart / order / payment）。若無效或未提供，停止並請使用者重新輸入。
2. `ddd@context-engineering-kit` plugin 規則已自動附加到 context

---

## Phase 0：DDD 分析

依序讀取以下文件，並萃取 **$ARGUMENTS** BC 的設計約束：

```
Read: docs/domain/bounded-contexts.md
Read: docs/domain/ubiquitous-language.md
Read: docs/domain/invariants.md
Read: docs/domain/domain-events.md
```

讀取完畢後，輸出以下分析摘要並等待確認：

```
## [$ARGUMENTS] 分析摘要

### Aggregate
- Aggregate Root:
- Entities:
- Value Objects:

### Invariants（本 BC）
- [規則] → 違反拋 [ExceptionName]

### Domain Events
- 發布:
- 訂閱:

### Port 清單
- In (UseCase):
- Out (Repository/Port):
```

**Gate G0**：上述 4 項確認無誤後才繼續。

---

## Phase 1：領域設計

規劃以下 package 結構（**只列出，不寫程式碼**）：

```
com.example.claudecodeclidemo.$ARGUMENTS/
├── domain/
│   ├── entity/
│   ├── vo/
│   ├── event/
│   └── exception/
├── application/
│   └── port/
│       ├── in/
│       └── out/
└── adapter/
    ├── in/
    │   └── web/
    └── out/
        └── persistence/
```

列出所有將要建立的 class 清單（含型別：Aggregate Root / Entity / VO / UseCase / Port / Event / Exception）。

等待使用者確認設計後才進 Phase 2。

---

## Phase 2：Red（寫測試，必須 FAIL）

**使用 `ecc:tdd-workflow` skill 指引。**

依序撰寫：
1. Value Object 單元測試
2. Domain Entity 不變量測試（每條 Invariant 至少一個負向案例）
3. Domain Entity 狀態轉換測試
4. Domain Event 發布測試
5. Use Case 單元測試（Mock Port Out）

測試撰寫完畢後執行：
```bash
./gradlew test
```

**確認所有測試 FAIL，且失敗原因是「實作不存在」（非 syntax error）。**

建立 Git checkpoint：
```bash
git add src/test/
git commit -m "test: add red tests for $ARGUMENTS"
```

回報 RED 結果（幾個測試、哪些 fail）後等待確認。

---

## Phase 3：Green（最小實作通過測試）

**`ddd` plugin 規則強制（`functional-core-imperative-shell`）：Domain 層只允許 `java.*` import，禁止 Spring / JPA。**

> **範圍**：Domain 層 + Port Interface。Application Service 完整實作保留至 Phase 5。

依序實作：
1. Value Objects（含驗證、equals、hashCode）
2. Domain Exceptions（對應 invariants.md）
3. Domain Entities / Aggregate Root（含狀態機、不變量守衛）
4. Domain Events（immutable record/class，含 `occurredAt`）
5. Port Interfaces（`application/port/in` 和 `application/port/out`）

實作完畢後執行：
```bash
./gradlew test
```

**確認所有測試 PASS。**

建立 Git checkpoint：
```bash
git add src/main/java/ src/test/java/
git commit -m "feat: implement $ARGUMENTS domain layer"
```

回報 GREEN 結果後等待確認。

---

## Phase 4：Refactor

**參照 `ddd` plugin 規則：`domain-specific-naming`、`function-file-size-limits`、`early-return-pattern`、`explicit-control-flow`、`command-query-separation`。**

檢查並修正：
- 無 `Utils` / `Helper` / `Manager` 類別名稱
- 命名符合 `docs/domain/ubiquitous-language.md` 詞彙表
- 每個方法 ≤ 20 行，每個 class ≤ 200 行
- 錯誤路徑使用 Early Return
- 查詢方法無副作用，命令方法無回傳值（CQS）
- Domain 層確認無 Spring / JPA import

重構後執行：
```bash
./gradlew test
```

**確認測試仍全綠。**

建立 Git checkpoint：
```bash
git commit -m "refactor: clean up $ARGUMENTS domain layer"
```

---

## Phase 5：Application Layer 完整實作

> Phase 3 僅建立 Port Interface；**Application Service 的完整實作在此 Phase 進行。**

實作 Application Service：
- `@Service @Transactional`（事務邊界在此層）
- implements UseCase Interface
- 注入 Port Out（Repository、外部 Port）
- 協調：呼叫 Port Out → 建立 Aggregate → 持久化 → 發布 Domain Event
- **Service 本身不含業務邏輯**

補充 Application Layer 完整測試（`@ExtendWith(MockitoExtension.class)`，Mock Port Out，不啟動 Spring context）。

詳細範本見 `docs/agent-workflow-ddd-tdd.md` §5.1–5.3。

---

## Phase 6：Adapter Layer

**Adapter In（Web）：**
- `@RestController`：薄薄一層，只做 DTO ↔ Command/Query 轉換
- `@WebMvcTest` 測試

**Adapter Out（Persistence）：**
- JPA Entity（與 Domain Entity 完全分離，只在 `adapter/out/persistence/` 下有 JPA annotation）
- Repository Adapter 實作 Port Out Interface
- `@DataJpaTest` 或 `@SpringBootTest` Integration Test

---

## Phase 7：測試驗收

執行全套測試與 Coverage 報告：
```bash
./gradlew test jacocoTestReport
```

確認 6 項驗收 Gate：

| # | 項目 | 門檻 |
|---|---|---|
| V-1 | 所有測試通過 | 0 failures |
| V-2 | Line Coverage | ≥ 60%（整體），domain ≥ 90% |
| V-3 | Domain 純度 | domain/ 無 Spring / JPA import |
| V-4 | 命名合規 | 符合 ubiquitous-language.md |
| V-5 | Invariant 測試 | 每條 Invariant 有負向測試 |
| V-6 | Event 測試 | 每個 Domain Event 有驗證發布的測試 |

若有項目未達標，回到對應 Phase 修正。

---

## Phase 8：開發報告

產出報告至 `docs/dev-reports/$ARGUMENTS-report.md`，包含：

- 實作摘要（Aggregate、UseCase、Domain Event 清單）
- TDD 循環記錄（每個循環的 RED / GREEN / REFACTOR commit hash）
- 覆蓋率數字（domain / application / adapter / 整體）
- DDD Rules 合規結果（規則各自 PASS / FAIL）
- 已知限制與後續待辦

---

## 執行守則

- 每個 Phase 結束後**回報進度並等待確認**，不自動跳到下一 Phase
- Phase 2 的 RED 必須實際執行測試確認 FAIL，不得跳過
- Phase 3 的 GREEN 必須實際執行測試確認 PASS，不得跳過
- Domain 層違反 DDD 規則時立即停止，修正後再繼續
- Git checkpoint 是必要步驟，不得省略
