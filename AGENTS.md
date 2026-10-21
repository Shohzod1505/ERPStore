# AGENTS.md

## Project
- Offline-first mini-ERP Android app for a beverage shop. Single module `:app`, package `ru.itis.kpfu.ermstore`.
- Current state: implemented MVP with Compose UI, Hilt, Room, Navigation, repositories, use cases, demo seeding, and JVM calculation tests. Runtime flows have been smoke-tested on an emulator.
- Target: products, warehouses/subwarehouses, purchase/sale/inventory documents, cash operations, and stock/sales/P&L reports.
- MVP scope is intentionally small. Do not add features outside `docs/requirements.md`; ask before expanding scope.
- Delivery: debug APK plus JVM unit tests for calculations. No README, screenshots, or CI.

## Commands
Run from the repo root (Windows/PowerShell):
- Build: `.\gradlew.bat assembleDebug`
- Unit tests: `.\gradlew.bat testDebugUnitTest`
- One test: `.\gradlew.bat testDebugUnitTest --tests "ru.itis.kpfu.ermstore.domain.calc.CalculationTest"`
- Lint: `.\gradlew.bat lint`
- Instrumented tests (device/emulator required): `.\gradlew.bat connectedDebugAndroidTest`

No formatter or typecheck task is configured. `assembleDebug` and `testDebugUnitTest` are the default verification.

## Toolchain
- Gradle 9.4.1. Daemon JVM 21 is pinned in `gradle/gradle-daemon-jvm.properties` and auto-provisioned via foojay. Local `java -version` may be 17; that is fine.
- AGP 9.2.1 with built-in Kotlin. Only `com.android.application` and `org.jetbrains.kotlin.plugin.compose` are applied. Do not add `org.jetbrains.kotlin.android`, `composeOptions`, or `kotlinCompilerExtensionVersion`.
- `app/build.gradle.kts` uses `compileSdk { version = release(37) }`. Keep this DSL; do not replace it with `compileSdk = 37`. Compile SDK 37 is required by current AndroidX dependencies; `targetSdk` stays 36.
- Java source/target 11, `minSdk 24`, `targetSdk 36`.
- Existing Kotlin and Gradle files use tabs. Match them.

## Dependencies
- All versions live in `gradle/libs.versions.toml`; reference `libs.*` aliases. Never hardcode coordinates or versions in `app/build.gradle.kts`.
- Pre-approved for the MVP: Compose BOM/Material3, Navigation Compose, Hilt, Room (+KSP), Coroutines/Flow, lifecycle-viewmodel-compose. Ask before adding anything else.
- Hilt is mandatory; do not replace it with manual DI. Room and Hilt use KSP. Verify the supported AGP 9 setup before adding; do not copy kapt/kotlin-android setup from older tutorials.

## Architecture
- Layers: `ui -> domain -> data`. UI and ViewModels must not touch DAOs or Room entities.
- `domain`: models, repository interfaces, use cases. `data`: Room entities/DAOs, repository implementations, mappers. `ui`: Compose screens, ViewModels, immutable UiState.
- Room is the single source of truth. DAO queries return `Flow`; ViewModels expose `StateFlow<UiState>`. Use Hilt constructor injection.
- Multi-table writes (posting documents, inventory adjustments) must run in a Room transaction.
- Do not store derived balances. Stock in units is the source of truth; blocks are derived from `unitsPerBlock`.
- Store money as `Long` kopecks and dates as `Long` epoch millis. Do not use `java.time` (minSdk 24, no desugaring).

## Domain rules
- Product quantity is stored in units (pieces). Never store a separate block balance. Blocks = `units / unitsPerBlock`.
- A product has only a name and `unitsPerBlock`; prices are entered per document line. Stock may live directly on a warehouse or on its subwarehouse.
- Documents have statuses `DRAFT`, `POSTED`, `CANCELLED`. A document affects stock or cash only when `POSTED`.
- Each document belongs to exactly one storage location. Reports have no date filters and cover all time.
- Drafts can be deleted; posted documents are immutable. Do not implement cancellation of posted documents.
- Posting a purchase increases stock; posting a sale decreases it. Posting an inventory creates a correction movement, never overwrites a balance.
- Inventory pre-fills all products at the selected location. Shortages are a P&L expense; surpluses are other income.
- Block a sale if stock at the selected location is insufficient; return an explicit error instead of crashing.
- Costing: weighted average cost per product. Update it on purchase; snapshot COGS on each sale line at posting time.
- Cash operations: `INCOME`, `EXPENSE`, `DEBT_GIVEN`, `DEBT_RECEIVED`. Posting a sale or purchase creates the corresponding cash entry. There is one cash register; currency is RUB.
- Loans are not profit or expense. P&L excludes them; cash flow includes them.
- Manual `INCOME` is other income in P&L; manual `EXPENSE` is an operating expense.
- P&L is calculated from posted sales, COGS snapshots, manual income/expense, and inventory adjustments. Never store a final profit value.

## Documentation and prompts
- Append development-related user prompts verbatim to `docs/ai-prompts.md` before implementing the request. Do not log setup/meta questions about the agent itself.
- Keep `docs/architecture.md` updated when design or behavior changes, and `docs/user-guide.md` when user-facing flows change.
- UI text and user documentation are Russian; code identifiers are English.

## Context7
- Use Context7 for current Room/Compose/Hilt/Navigation/Kotlin APIs. Call `context7_resolve-library-id` first, then `context7_query-docs`.
- Known ID: Room `/websites/developer_android_training_data-storage_room`.

## Gotchas
- `local.properties` is tracked in git despite being in `.gitignore`; do not commit changes to it.
- There is no README, CI, formatter, or lint config; do not invent them.
- Do not modify unrelated files. Never commit unless explicitly asked.
- Do not introduce multi-module builds or `build-logic` for this MVP; keep a single `:app` module.
