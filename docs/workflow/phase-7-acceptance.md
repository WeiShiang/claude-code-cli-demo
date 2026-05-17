# Phase 7：測試驗收

> 主流程文件：`../agent-workflow-ddd-tdd.md`

## 1. 執行全套測試

```bash
# 單元 + 整合測試 + JaCoCo 報告
./gradlew test jacocoTestReport

# 開啟覆蓋率報告
# Windows
start build/reports/jacoco/test/html/index.html
```

---

## 2. Coverage Gate（JaCoCo）

| 層次 | 最低門檻 |
|---|---|
| Domain Layer | **≥ 90%** line coverage |
| Application Layer | **≥ 80%** line coverage |
| Adapter Layer | **≥ 70%** line coverage |
| 整體 | **≥ 60%** line coverage（專案設定） |

---

## 3. DDD 合規檢查（Domain 純度）

### Git Bash / Linux / macOS

```bash
grep -r "import org.springframework" src/main/java/com/example/claudecodeclidemo/*/domain/
grep -r "import jakarta.persistence" src/main/java/com/example/claudecodeclidemo/*/domain/
# 預期：無任何輸出
```

### Windows PowerShell

```powershell
Select-String -Recurse -Path "src\main\java\com\example\claudecodeclidemo\*\domain\" `
  -Pattern "import org.springframework","import jakarta.persistence"
# 預期：無任何輸出
```

---

## 4. 驗收 Gate

| # | 檢查項目 | 通過條件 |
|---|---|---|
| V-1 | 所有測試通過 | `BUILD SUCCESS`，0 failures |
| V-2 | Coverage 達標 | JaCoCo 報告 line coverage ≥ 60% |
| V-3 | Domain 純度 | Domain 層無 Spring / JPA import |
| V-4 | 命名合規 | 類別/方法名稱符合 `ubiquitous-language.md` |
| V-5 | Invariant 測試 | 每條 Invariant 有對應的負向測試 |
| V-6 | Event 測試 | 每個 Domain Event 有驗證發布的測試 |

若有項目未達標，回到對應 Phase 修正。
