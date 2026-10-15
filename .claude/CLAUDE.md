# OctoBoard

Kotlin Multiplatform GitHub client (Android + iOS, Compose Multiplatform) — a learning project.

## Where to look

- **`specs/octoboard.md`** — source of truth: features, tech stack, module architecture (`:core:*`, `:shared:*`, `:feature:*`, `:integration`), and the phased implementation roadmap. Implement it phase by phase, in order — don't jump ahead.
- **`README.md`** — build/run/test commands for the Android and iOS apps.
- **`settings.gradle.kts`** — actual current module list; compare against the spec's target module structure to see what's been built vs. still pending.

## Source layout

- `core/network/src/commonMain` — `:core:network`: Ktor `GithubApiClient`, platform HTTP engines, `AuthTokenProvider`
- `shared/*/src/commonMain` — `:shared:*`: domain-aware code shared by two or more features, so that `:core:*` stays domain-free: `:shared:entities` (`GithubRepository`), `:shared:github-api` (GitHub DTO/mapper, pagination), `:shared:ui` (repository list composables). Sits between `:core` and `:feature`. May depend on `:core:*` and on `:shared:entities` only (never on features or other `:shared:*`); Compose/UI is allowed only in `:shared:ui`.
- `feature/<name>/{entities,data,presentation}/src/commonMain` — `:feature:<name>:*`: each feature is split by layer. `:entities` (entities package), `:data` (data sources, repository, DTOs, SQLDelight schema, data Koin module), `:presentation` (presentation, ui, navigation, di). `:presentation → :data → :entities`. A feature may depend on another feature's `:data`/`:entities`, never on its `:presentation`; no cycles. Only create a submodule if the layer has code (`:feature:auth` has no `:entities`). `:feature:favorites:data` owns `FavoritesStore`, used by `:feature:repodetail:data`.
- `integration/src/commonMain` — `:integration`: umbrella module aggregating Koin modules and (from phase 3 on) nav entries; produces the iOS framework
- `androidApp/` — Android entry point
- `iosApp/` — iOS entry point (SwiftUI/Xcode project)

## Code style

- When calling a function with more than 1 parameter, put each parameter on its own line, and don't put any parameter on the same line as the call itself (i.e. nothing after the opening paren). A single-parameter call may stay on one line.
- Always use named arguments when a call has 2 or more parameters — never pass them positionally, even if none of the parameters were already named. Exemptions (all compiler-enforced, not stylistic — Kotlin rejects named arguments in these cases):
  - Invoking a value of function type, e.g. a lambda-typed constructor/callback parameter like `onRepoSelected(navigator, owner, repo)` ("Named arguments are prohibited for function types").
  - Calling a non-Kotlin (Java/ObjC) function or constructor, e.g. `Exception(message, cause)` on the Android/JVM target where `Exception` resolves to `java.lang.Exception` ("Named arguments are prohibited for non-Kotlin functions").
  - Vararg-style parameters like `stringResource`'s format args, which can't be meaningfully named at the call site.

## Visibility

- In `:feature:*:presentation` everything is `internal` except the feature's Koin module, its `NavEntries` class, the NavKeys other modules navigate to (plus their sealed parent interface) and what `:integration` needs for platform wiring. `:feature:*:entities` types are public. In `:feature:*:data` the repositories (internal constructor), shared stores (e.g. `FavoritesStore`, internal constructor) and the data Koin module are public; data sources and DTOs are `internal`. `:feature:auth` is the documented exception (`AuthViewModel`/`AuthUiState`, `AuthBottomSheetContent` in `:presentation`, `SecureStorage` in `:data` are public). New `UiState`s, entities and `xxxNavEntry(...)` functions start `internal`. DTOs in feature modules are always `internal`; DTOs in `:shared:*` modules may be public.

## Testing

Tests are out of scope for this project. Don't write new tests, and don't run test tasks (`./gradlew test`, etc.).
