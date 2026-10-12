# OctoBoard

Kotlin Multiplatform GitHub client (Android + iOS, Compose Multiplatform) — a learning project.

## Where to look

- **`specs/octoboard.md`** — source of truth: features, tech stack, module architecture (`:core:*`, `:shared:*`, `:feature:*`, `:integration`), and the phased implementation roadmap. Implement it phase by phase, in order — don't jump ahead.
- **`README.md`** — build/run/test commands for the Android and iOS apps.
- **`settings.gradle.kts`** — actual current module list; compare against the spec's target module structure to see what's been built vs. still pending.

## Source layout

- `core/network/src/commonMain` — `:core:network`: Ktor `GithubApiClient`, platform HTTP engines, `AuthTokenProvider`
- `shared/*/src/commonMain` — `:shared:*`: domain-aware code shared by two or more features, so that `:core:*` stays domain-free: `:shared:entities` (`GithubRepository`), `:shared:github-api` (GitHub DTO/mapper, pagination), `:shared:favorites-data` (favorites SQLDelight store), `:shared:ui` (repository list composables). Sits between `:core` and `:feature`. May depend on `:core:*` and on `:shared:entities` only (never on features or other `:shared:*`); Compose/UI is allowed only in `:shared:ui`.
- `integration/src/commonMain` — `:integration`: umbrella module aggregating Koin modules and (from phase 3 on) nav entries; produces the iOS framework
- `androidApp/` — Android entry point
- `iosApp/` — iOS entry point (SwiftUI/Xcode project)

## Code style

- When calling a function with more than 1 parameter, put each parameter on its own line, and don't put any parameter on the same line as the call itself (i.e. nothing after the opening paren). A single-parameter call may stay on one line.
- Always use named arguments when a call has 2 or more parameters — never pass them positionally, even if none of the parameters were already named. Exemptions (all compiler-enforced, not stylistic — Kotlin rejects named arguments in these cases):
  - Invoking a value of function type, e.g. a lambda-typed constructor/callback parameter like `onRepoSelected(navigator, owner, repo)` ("Named arguments are prohibited for function types").
  - Calling a non-Kotlin (Java/ObjC) function or constructor, e.g. `Exception(message, cause)` on the Android/JVM target where `Exception` resolves to `java.lang.Exception` ("Named arguments are prohibited for non-Kotlin functions").
  - Vararg-style parameters like `stringResource`'s format args, which can't be meaningfully named at the call site.

## Testing

Tests are out of scope for this project. Don't write new tests, and don't run test tasks (`./gradlew test`, etc.).
