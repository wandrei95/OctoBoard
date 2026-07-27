# OctoBoard

Kotlin Multiplatform GitHub client (Android + iOS, Compose Multiplatform) — a learning project.

## Where to look

- **`specs/octoboard.md`** — source of truth: features, tech stack, module architecture (`:core:*`, `:feature:*`, `:integration`), and the phased implementation roadmap. Implement it phase by phase, in order — don't jump ahead.
- **`README.md`** — build/run/test commands for the Android and iOS apps.
- **`settings.gradle.kts`** — actual current module list; compare against the spec's target module structure to see what's been built vs. still pending.

## Source layout

- `core/network/src/commonMain` — `:core:network`: Ktor `GithubApiClient`, platform HTTP engines, `AuthTokenProvider`
- `integration/src/commonMain` — `:integration`: umbrella module aggregating Koin modules and (from phase 3 on) nav entries; produces the iOS framework
- `androidApp/` — Android entry point
- `iosApp/` — iOS entry point (SwiftUI/Xcode project)

## Code style

- When calling a function with more than 1 parameter, put each parameter on its own line, and don't put any parameter on the same line as the call itself (i.e. nothing after the opening paren). A single-parameter call may stay on one line.
- If a call uses a named argument for at least one parameter, name all of its parameters — don't mix positional and named arguments in the same call.

## Testing

Tests are out of scope for this project. Don't write new tests, and don't run test tasks (`./gradlew test`, etc.).
