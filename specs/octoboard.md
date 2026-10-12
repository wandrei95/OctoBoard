# OctoBoard — KMP Learning Project Spec

A GitHub client built with Kotlin Multiplatform to learn KMP fundamentals: shared UI via Compose Multiplatform (Android + iOS), Ktor networking with interceptors, Koin DI, and Navigation 3 with adaptive list-detail layouts — all organized around **self-contained feature modules**.

This document is written as an implementation spec. Each section is scoped so it can be handed to an AI coding assistant (e.g. Claude Code) phase by phase — do not implement everything in one pass; follow the roadmap order.

---

## 1. Tech stack

| Concern | Library |
|---|---|
| UI | Compose Multiplatform 1.10+ |
| Navigation | Navigation 3 (`androidx.navigation3`, multiplatform via CMP 1.10+) |
| Adaptive layout | `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` |
| Networking | Ktor Client (`OkHttp` engine on Android, `Darwin` engine on iOS) |
| Serialization | kotlinx.serialization |
| DI | Koin (`koin-core`, `koin-compose`) |
| Local persistence | SQLDelight |
| Language/tooling | Kotlin 2.2.20+, Gradle version catalogs |

Target API: [GitHub REST API v3](https://docs.github.com/en/rest) (`https://api.github.com`), unauthenticated by default, optional PAT for higher rate limits and user-specific data.

---

## 2. Architecture principle

**Every feature module is self-contained.** A feature module owns, in its own `commonMain`:

- its Compose screens (UI)
- its ViewModels
- its domain models / DTOs
- its repository (built on top of generic `core` infrastructure)
- its Navigation 3 destinations (`NavKey`s + entry registrations)
- its own Koin module
- its own local-persistence schema, if it needs one

`core:*` modules contain **only genuinely non-feature-specific infrastructure** — things with no domain opinion (HTTP plumbing, DB driver setup, generic reusable UI components, navigation scaffolding). A `core:*` module must never depend on a `:feature:*` module. A `:feature:*` module may depend on `core:*` and `shared:*` modules, but never on another `:feature:*` module directly — cross-feature navigation happens through `NavKey`s, not direct imports.

`shared:*` modules sit between `core:*` and `:feature:*`. They hold **domain-aware data, logic and UI needed by two or more features** — the things that are not a core concept (so `core:*` stays domain-free) but are too shared to duplicate per feature: `:shared:entities` (the `GithubRepository` entity), `:shared:github-api` (GitHub DTO, mapper and pagination), `:shared:favorites-data` (favorites persistence) and `:shared:ui` (the repository list composables). Rules: a `shared:*` module may depend on `core:*`, and on `:shared:entities` (the lowest shared module) — no other `shared:* → shared:*` edge, and never a `:feature:*`; UI (Compose) is allowed only in `:shared:ui`, every other shared module is UI-free; and a module belongs there only if at least two features need it — single-feature code stays in its feature.

An **umbrella module (`:integration`)** sits above the feature modules and below the platform apps. It is the only module (besides the platform apps) allowed to depend on every feature module. Its job is purely aggregation: it merges each feature's Koin module list, each feature's nav entries, and each feature's `NavKey` serializers into one Koin module list and one `NavDisplay` setup that the platform apps consume.

```
              :androidApp        :iosApp
                     \             /
                      :integration
                             |
      -----------------------------------------------
      |         |            |          |
:feature:   :feature:   :feature:    :feature:    :feature:
 search     repodetail  favorites   userprofile   auth
      |         |            |          |            |
      -----------------------------------------------
                             |
   :shared:entities  :shared:github-api  :shared:ui  :shared:favorites-data
                             |
      -----------------------------------------------
      |            |             |              |             |
:core:network  :core:database  :core:designsystem  :core:ui-nav  :core:common
```

### 2.1 Internal layering within a module

Every module that owns UI (every `:feature:*` module, plus `:integration`) is internally organized into six layers, each in its own package, with a strict one-way dependency chain:

```
di → navigation → ui → presentation → data → entities
```

A layer may only depend on layers to its right. Concretely:

- **`entities`** — plain domain models (POJOs). No serialization annotations, no framework imports (no Ktor, no SQLDelight, no Koin, no Compose). Feature-owned by default, per §4.5's duplication-over-coupling principle — not shared across features. Two exceptions: a type with **no domain opinion at all** (e.g. a generic `PagedResult<T>`) belongs in `:core:common/entities` (§2.2), and `GithubRepository` is shared via `:shared:entities` (§4.5). Any other type that represents a GitHub concept (a user, an issue, ...) stays feature-owned and duplicated, even if two features' versions currently look identical.
- **`data`** — talks to `core:network` / `core:database`. Owns wire-format DTOs (`@Serializable`, always `internal` — a feature's DTOs never leave the feature; a DTO that several features need lives in a `:shared:*` module instead, see below), one or more data-source classes, and a repository class. A **remote data source** is the only thing that talks to `HttpClient` directly: it owns the actual shape of the API call (path, query params, headers, method), turns the raw response into a DTO, and maps that DTO into an `entities` type before returning — so a data source's success value is always an `entities` type, never a DTO; the DTO never leaves `data`. Every remote data source function is wrapped in `core:network`'s `safeApiCall` (§6.4) and returns `Result<T>`, `T` being (or wrapping) an `entities` type. A **repository is purely a data-source orchestrator**: it holds one or more data sources (e.g. a `XyzRemoteDataSource`, later a `XyzLocalDataSource`), calls them, and composes their already-`entities`-typed results into what its own public function returns — for a single data source with nothing further to combine, that's a straight pass-through (see §6.5); it never imports Ktor/SQLDelight itself, never sees an `HttpClient`/`HttpResponse`, and never touches a DTO. Every repository function returns `Result<entities-type>`, propagated via `Result.map`/`mapCatching` — never unwrapped with `getOrThrow()` inside `data`. Repositories and data sources are concrete classes; no interface/impl split unless a second implementation genuinely exists. Once a feature has more than a couple of `data` files, split it into `repositories/`, `datasources/`, and `models/` subpackages rather than leaving everything flat — see §6.5 for the convention.
- **`presentation`** — ViewModels and UI-state classes. A ViewModel takes its `data`-layer dependencies via plain constructor injection — it never resolves anything from Koin itself. Exposes state as `StateFlow<UiState>`.
- **`ui`** — Compose screens only. Takes its ViewModel directly as a parameter (reads `collectAsState()`, calls VM methods directly) plus plain navigation-callback lambdas (e.g. `onRepoSelected: (owner: String, repo: String) -> Unit`). No Koin import. No navigation logic beyond invoking the callbacks it's handed — the screen doesn't know what a callback does or where it leads. No hardcoded user-facing strings either — every literal a composable renders comes from a Compose Multiplatform string resource (§2.3). No hardcoded spacing/sizing `.dp` literals either — every such value comes from `OctoBoardTheme.dimensions` (§2.4).
- **`navigation`** — owns this module's `NavKey`s and its `FeatureNavEntry`/entry registration (see §7). This is where `koinViewModel()` (or `koinInject()`) is called to resolve a screen's ViewModel, and where the `ui`-layer composable is invoked with that ViewModel plus navigation lambdas wired to `OctoBoardNavigator`.
- **`di`** — this module's Koin module definition(s) only (see §8).

**Hard rule: `ui`, `presentation`, `data`, and `entities` must never import anything from `org.koin.*`.** Only `di` and `navigation` are Koin-aware. This keeps every layer below `navigation` trivially constructible and testable without a DI container.

**Scope:** `core:*` modules have no screens or nav destinations, so they only carry a `di` package (their Koin module, per §8.2's pattern) — everything else in a `core:*` module stays flat at its root package. The full six-layer split applies to `:feature:*` modules and to `:integration`.

**Platform composition roots are exempt.** `:androidApp`'s `Application`/`Activity` classes and `:integration`'s iOS `MainViewController.kt` (§8.5) are where the whole apparatus is bootstrapped — they legitimately call into both `di` (starting Koin) and `navigation` (invoking the root composable) in one place, and aren't required to live inside a layer package themselves.

### 2.2 `core:common`

`core:common` is the one place genuinely feature-agnostic code is allowed to live instead of being duplicated per feature. It's organized into topic subpackages at its root (`entities/` today) rather than the six-layer split, since it has no UI/nav/DI of its own.

**The rule that keeps this from becoming a dumping ground: everything in `core:common` must be framework-free** — no Koin, no Ktor, no SQLDelight, no Compose. That's what makes it safe to depend on from any layer, including `entities` (§2.1's hard rule against framework imports in `entities` would otherwise be violated the moment a feature's `entities` package depended on something Compose- or Ktor-aware). If a future shared *thing* genuinely needs a framework — e.g. a reusable Compose component — it belongs in `core:designsystem`, not here.

Litmus test before adding something here: would this type make sense in a project with a completely different feature set? `PagedResult<T>` passes (any paginated list needs it). Most GitHub-shaped domain types fail this test and stay feature-owned per §4.5 — a GitHub issue or pull request, say, would still belong to whichever feature introduced it, even if a second feature later wanted an identical-looking shape.

**`GithubRepository` is not here.** It is inherently "a GitHub repo," so it fails this litmus test and lives in `:shared:entities` (§3) — the one domain entity deliberately shared across features, for the reasons in §4.5. `core:common` itself stays domain-free.

### 2.3 String resources — no hardcoded strings in `ui`

Every piece of static, user-facing copy a composable renders — field labels, button/segment text, empty/loading/no-results messages, dropdown option labels, etc. — is a Compose Multiplatform string resource, never a string literal passed straight to `Text(...)` or a `core:designsystem` component. `:feature:search`'s `ui` package (`SearchScreen.kt`) is the reference implementation for this pattern:

- Each feature owns its own `src/commonMain/composeResources/values/strings.xml`, one `<string>` entry per piece of copy, with the key **prefixed by the feature name** (e.g. `search_query_label`, `search_sort_best_match`, `search_empty_state_message`) — this avoids key collisions once `:integration` links every feature's resources together into the app.
- The feature's `build.gradle.kts` sets a feature-scoped resource package and pulls in the resources artifact:
  ```kotlin
  compose.resources {
      packageOfResClass = "com.test.kmp.octoboard.feature.search.generated.resources"
  }
  // commonMain.dependencies
  implementation(libs.compose.components.resources)
  ```
- In `ui` code, a string is resolved with `stringResource(Res.string.<key>)`, imported from that feature's generated `Res` object (`com.test.kmp.octoboard.feature.search.generated.resources.Res`) alongside one import per key used. Because `stringResource` is itself `@Composable`, any helper that reads one becomes `@Composable` too — e.g. `SearchScreen.kt`'s private `SearchSort.label()` extension, which exists purely to map an enum value to its resource string.
- **Exempt from this rule:** data that already comes from the API/domain layer rather than being UI copy — rendering `repo.fullName`, `repo.language`, or a star count is display of runtime data, not a hardcoded string, so it's fine as a plain value inside `Text(...)`. This includes error text that already reaches `ui` as a `String` on a state class (e.g. `SearchStatus.Error.message`) — the *fallback* copy for such a message, if a feature's `presentation`/`data` layer needs one, is a separate concern from this section and isn't covered by the `ui`-only rule here.

This applies to every current and future `:feature:*` module and to `:integration` — no screen should introduce a new string literal where a resource entry belongs.

### 2.4 Dimension tokens — no hardcoded `.dp` literals in `ui`

Every spacing or sizing value a composable uses — padding, `Spacer` sizes, `Arrangement.spacedBy`, component dimensions — is read from `OctoBoardTheme.dimensions`, never a raw `<n>.dp` literal typed inline. `core:designsystem` defines this the same way Material3 itself defines `MaterialTheme`:

- `OctoBoardDimensions.kt` declares a single `data class OctoBoardDimensions` of named `Dp` tokens, grouped by category — `spacingExtraSmall`/`spacingSmall`/`spacingMedium`/`spacingLarge`/`spacingExtraLarge` for spacing; `avatarSizeMedium`, `iconSizeSmall`, etc. for component sizing; `cornerRadiusSmall`/`cornerRadiusMedium` reserved for a future custom `Shape` — held in a `staticCompositionLocalOf`.
- `OctoBoardTheme.kt` exposes it via `object OctoBoardTheme { val dimensions: OctoBoardDimensions @Composable get() = ... }`, sitting alongside the existing `@Composable fun OctoBoardTheme(content)` that wraps `MaterialTheme` and provides the `CompositionLocal`. An `object` and a same-named top-level `@Composable fun` can coexist in one file — Kotlin resolves a bare reference (`OctoBoardTheme.dimensions`) against the object and a call (`OctoBoardTheme { ... }`) against the function — this is the exact trick Material3 uses for `MaterialTheme` itself (an `object MaterialTheme` for `.colorScheme`/`.typography`/`.shapes`, plus a same-named `@Composable fun MaterialTheme(...)` for the theme root).
- Call sites read `OctoBoardTheme.dimensions.spacingMedium` etc. — e.g. `Modifier.padding(OctoBoardTheme.dimensions.spacingLarge)`, `Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingSmall))`. Keep spacing and sizing tokens semantically distinct even where their values coincide (both `spacingExtraLarge` and `iconSizeSmall` happen to be `24.dp` today) — they mean different things, and collapsing them into one shared token would make changing one accidentally change the other.
- `core:designsystem`'s own components (`Avatar`, `EmptyState`, `ErrorState`, `InfoChip`) use these tokens too, including in default parameter values (`size: Dp = OctoBoardTheme.dimensions.avatarSizeMedium`) — the module that defines the tokens dogfoods them rather than keeping its own literals.

This applies to every current and future `:feature:*` module's `ui` package and to `core:designsystem` itself — no new hardcoded `.dp` value should be introduced where a dimension token belongs.

---

## 3. Module structure


```
:core:common          - Framework-free, domain-free shared code only (see §2.2): entities/
                        today (PagedResult<T>). No Koin/Ktor/SQLDelight/Compose imports
                        ever, and no GitHub or other domain models.
:core:network        - Ktor HttpClient factory (expectSuccess = true), interceptors,
                        AuthTokenProvider interface, ApiError sealed class + safeApiCall
                        (§6.4) — the one place HTTP failures become Result. No domain
                        models and no per-endpoint request helpers: every feature's own
                        remote data sources call the HttpClient directly, wrapped in
                        safeApiCall (§6.5)
:core:database         - SqlDriver factory (expect/actual) ONLY — no tables, no DTOs.
                        Any module that needs local storage asks this module for a
                        driver and defines its own SQLDelight schema.
:core:designsystem     - shared Compose theme, typography, and generic reusable
                        components (buttons, cards, loading/error/empty states,
                        avatar, chips). Domain-free: it does not know GithubRepository
                        (the repository list lives in :shared:ui). NEVER a full screen.
:core:ui-nav           - Navigation 3 scaffolding: the NavDisplay host, adaptive scene
                        strategy setup, and the FeatureNavEntry contract every feature
                        implements. Contains no feature NavKeys itself.

:shared:entities      - GithubRepository, the one domain entity shared across features
                        (see §4.5). Plain data class, no dependencies.
:shared:favorites-data - Favorites persistence shared by :feature:favorites and
                        :feature:repodetail: the SQLDelight schema (FavoriteRepositoryId.sq —
                        a minimal table of favorited ids only, db file favorites.db), built
                        on core:database's driver factory, and a public FavoritesStore (the
                        only class that touches the generated database; observeFavoriteIds,
                        observeIsFavorite(id): Flow<Boolean>, setFavorite) + its Koin module.
                        Data only — no UI.

:shared:github-api     - GitHub REST API knowledge shared by features' remote data
                        sources: the repository DTO + its mapping to GithubRepository
                        and Link-header pagination (hasNextPage). Its DTOs are public (the exception to
                        "DTOs are internal": they exist to be used by other modules).
                        Depends on :shared:entities. No UI.

:shared:ui             - GithubRepositoryList / GithubRepositoryListItem, the repository
                        list shared by search and favorites (built on :core:designsystem's
                        Avatar/InfoChip/theme; depends on :shared:entities). The only
                        shared module allowed to contain UI.

:feature:search        - SearchScreen, SearchViewModel, Github*SearchResponseDto DTOs,
                        GithubRepositorySearchRemoteDataSource + GithubUserSearchRemoteDataSource
                        (own the actual Ktor calls, each wrapped in safeApiCall), SearchRepository
                        (orchestrates the two data sources, returns Result<entities>),
                        Search NavKeys + entries, searchKoinModule
:feature:repodetail    - GithubRepositoryDetailScreen, GithubRepositoryDetailViewModel,
                        GithubRepositoryDetail* response DTOs (data/models, wire format),
                        GithubRepositoryDetailRepository (orchestrates the remote data source
                        plus the shared FavoritesStore), GithubRepositoryDetail NavKeys +
                        entries, githubRepositoryDetailKoinModule.
                        Shows and toggles favorite state, scoped narrowly to a single on/off
                        toggle: a star icon inline in the detail screen's header content (this
                        screen has no TopAppBar) — the only place a repo can be favorited/
                        unfavorited. Favorite state is a feature-owned entity,
                        GithubRepositoryDetail (entities/ — distinct from the response DTOs
                        above), wrapping the shared GithubRepository plus isFavorite: Boolean,
                        rather than mutating the shared entity itself. It reads the live flag
                        from :shared:favorites-data's FavoritesStore
                        (observeIsFavorite(id): Flow<Boolean>), so
                        GithubRepositoryDetailRepository.getRepositoryDetails returns
                        Flow<Result<GithubRepositoryDetail>> — one one-shot network fetch
                        combined with the live favorite flow — so toggling favorite propagates
                        back to the UI with no manual ViewModel state patch (§6.5).
:feature:favorites     - FavoritesScreen, FavoritesViewModel, FavoritesRepository (combines
                        FavoritesStore's saved ids with FavoritesRemoteDataSource's
                        GET /repositories/{id}, in parallel, no caching; failures are counted
                        and surfaced as a note), FavoritesNavEntryKey + entries,
                        favoritesKoinModule. Opened from the FAB menu in :integration;
                        selecting a repo opens the normal detail pane.
:feature:userprofile   - same pattern
:feature:auth          - AuthScreen, AuthViewModel, SecureStorage (expect/actual),
                        implements AuthTokenProvider (from core:network), Auth NavKeys
                        + entries, authKoinModule

:integration            - umbrella module: aggregates every feature's Koin module list,
                        NavKey serializers, and nav entries into one Koin module list
                        and one NavDisplay setup

:androidApp             - Activity, Android platform Koin module (platform expect/actual
                        factories), depends on :integration
:iosApp                  - ComposeUIViewController entry point, iOS platform Koin module,
                        depends on :integration
```

### Resolving the one real cross-cutting dependency: auth token

`core:network`'s auth interceptor needs a token, but token storage is owned by `:feature:auth` — and `core:*` can't depend on `:feature:*`. This is solved with dependency inversion:

- `core:network` declares a small interface: `interface AuthTokenProvider { fun getToken(): String? }`
- `:feature:auth`'s `SecureStorage` implements it (or a thin adapter wraps it)
- `:integration` binds the concrete implementation to the interface in Koin, so `core:network`'s interceptor only ever depends on the abstraction

This is a good, small, realistic KMP/DI lesson — treat it as one, don't just wire it silently.

---

## 4. Features

### 4.1 `:feature:search`
- Debounced search-as-you-type against `GET /search/repositories` and `GET /search/users`
- Pagination via GitHub's `Link` response header
- Sort/filter controls: stars, language, last updated
- Uses the shared `:shared:entities` `GithubRepository` entity for repo results; owns its own `GithubUserSummary` entity for user results (see §4.5 on shared-shape duplication — user summaries aren't consolidated the same way)
- List pane in the adaptive list-detail scaffold; navigates to `GithubRepositoryDetailKey` / `UserProfileKey` from other features

### 4.2 `:feature:repodetail`
- `GET /repos/{owner}/{repo}`, `GET /repos/{owner}/{repo}/languages`, `GET /repos/{owner}/{repo}/contributors`
- Uses the shared `:shared:entities` `GithubRepository` entity (wrapped, for this screen, in the feature-owned `GithubRepositoryDetail` entity described below); owns its own `GithubRepositoryLanguageBreakdown`, `GithubRepositoryContributor` DTOs
- Detail pane in the adaptive list-detail scaffold; contributors optionally shown in a third "extra" pane on very wide layouts
- **Shows and toggles favorites, scoped narrowly to a single on/off toggle.** A star icon (`Icons.Filled.Star` / `StarBorderIcon`, from `org.jetbrains.compose.material:material-icons-core` — versioned independently of the Compose Multiplatform plugin, added as `libs.compose.material.icons.core`) sits inline in the detail screen's header content — the *only* place a repo can be favorited/unfavorited. There is no `TopAppBar` on this screen. The favorites data itself lives in `:shared:favorites-data` (§3); the favorites-list screen is its own module, `:feature:favorites`, which reads all saved ids, fetches each repo via `GET /repositories/{id}` in parallel on every screen entry and whenever the id set changes (no caching; failures are counted and surfaced as a "N favorites couldn't be loaded" note), and renders them with `core:designsystem`'s shared `GithubRepositoryList`. It is opened from an app-level FAB menu in `:integration` (Favorites / PAT entries, `IntegrationFabMenu`) and selecting a repo opens the normal detail pane. A favorite-sourced detail entry point that reads the base repo locally is still deferred
- Favorite state is a feature-owned entity, `GithubRepositoryDetail(repository: GithubRepository, isFavorite: Boolean)` in `entities/` — not a mutation of the shared `GithubRepository` entity itself, since favorites is a repodetail-only concern and shouldn't leak into the entity every other feature consumes (§2.2)
- Persisted by `:shared:favorites-data` (its own SQLDelight schema, `FavoriteRepositoryId.sq` — a minimal table of favorited ids only, not a full repo mirror — built on `core:database`'s driver factory); its public `FavoritesStore` is the only class that touches the generated database, mirroring how a remote data source is the only thing that touches `HttpClient` (§6.5). `GithubRepositoryDetailRepository` takes the store directly and exposes `setFavorite` for the star toggle
- `FavoritesStore` (`:shared:favorites-data`) is reactive: `observeIsFavorite(id: Long): Flow<Boolean>` plus `suspend fun setFavorite(id: Long, isFavorite: Boolean)`. `GithubRepositoryDetailRepository.getRepositoryDetails` combines the store with the remote data source, returning `Flow<Result<GithubRepositoryDetail>>` — one one-shot network fetch joined with the live favorite flow, so toggling favorite propagates back to the UI with no manual ViewModel state patch (§6.5). `getLanguages`/`getContributors` stay one-shot `suspend` functions, unchanged

### 4.3 `:feature:userprofile`
- `GET /users/{username}`, `GET /users/{username}/repos`
- Owns its own `GithubUserDetails` DTO; its repo list (`GET /users/{username}/repos`) uses the shared `:shared:entities` `GithubRepository` entity, same as search and repo detail
- Reachable from search results and from contributor avatars in repo detail (via `NavKey`, not direct module dependency)

### 4.4 `:feature:auth`
- User enters a GitHub PAT
- Owns `SecureStorage` (`expect`/`actual`: EncryptedSharedPreferences/DataStore on Android, Keychain on iOS) and implements `AuthTokenProvider`

### 4.5 On duplicated domain shapes across features — and the one exception

The general rule in this project is duplication over coupling: when two features both need "a GitHub X," each defines its **own** shape for its own screen, mapped independently from the raw JSON, rather than reaching for something shared. This is deliberate, not an oversight — it's the standard trade-off of self-contained feature modules, and it's worth experiencing directly as part of this learning project. This does **not** apply to types with no domain opinion at all (see §2.2's `PagedResult<T>` example) — those belong in `core:common` from the start, not after the fact. But for an actual domain shape, don't extract it just because two features' versions currently look identical — `core:network` in particular is the wrong target regardless (§3: it carries no domain models by design). Only fold a domain shape into something shared once the duplication is genuinely painful, and even then, weigh it carefully against the coupling it introduces.

**`GithubRepository` is the one exception**, and it's worth spelling out why, since it looks at first glance like exactly the case the paragraph above warns against. Search results, repo detail, and user-profile repo lists all originally defined their own shape (`GithubRepositorySummary`, `GithubRepositoryDetails`, `GithubUserRepositorySummary`) with slightly different fields each. But a direct comparison of the actual GitHub REST API responses behind `GET /search/repositories`, `GET /users/{username}/repos`, and `GET /repos/{owner}/{repo}` showed the three per-feature shapes weren't reflecting genuinely different views of a repo — they were three different subsets parsed from what is, at the wire level, one shared schema: the first two endpoints return GitHub's `repository` object identically, and the third returns `full-repository`, a strict superset. The duplication wasn't domain modeling, it was an accident of which fields each DTO happened to parse. That's a fundamentally different situation from "two features' versions happen to look similar today" — the underlying resource genuinely is one thing — which is why `GithubRepository` was consolidated into `:shared:entities` (§3) — together with its DTO and mapper in `:shared:github-api` and its list composables in `:shared:ui` — instead of staying split three ways (the shared `GithubRepositoryDto` is public for that reason; DTOs inside a feature module stay `internal`), using only the union of fields already in use (no speculative fields like `topics`/`license` pulled in just because the API offers them).

This is a named, one-time exception for the app's single most central entity — not a general license to consolidate the next domain shape that looks similar. Absent the same kind of proof (the same wire-level resource, not just a coincidental field overlap), new domain shapes should still default to feature-owned duplication.

---

## 5. Roadmap (implement in this order)

1. **Skeleton** — KMP project, CMP targets for Android + iOS, `ComposeUIViewController` entry point, `core:network`'s `HttpClient` factory + platform engines, one smoke-test call rendered as raw text on both platforms, empty `:integration` module wired into both apps.
2. **Search, fully self-contained** — `:feature:search` with its own UI, ViewModel, DTOs, repository, NavKey, Koin module. `:integration` aggregates just this one feature's Koin module and nav entry to prove the umbrella pattern works end to end before adding more features.
3. **Navigation 3 + adaptive, cross-feature** — add `:feature:repodetail`, wire `SearchKey → GithubRepositoryDetailKey` navigation through `:integration`'s combined `NavDisplay` and `SerializersModule`. Introduce `ListDetailSceneStrategy`.
4. **User profile** — add `:feature:userprofile`, cross-navigate from both search and repo detail (contributors).
5. **Auth + interceptors** — add `:feature:auth`, `AuthTokenProvider` interface in `core:network`, bind the concrete implementation in `:integration`.
6. **Favorites + SQLDelight** — add `:core:database` (SqlDriver factory only) and `:shared:favorites-data` (SQLDelight schema + `FavoritesStore`, built on that driver factory), then a star toggle inline in the repo detail screen's header (the only place to favorite/unfavorite), backed by a minimal id-only local table and the reactive `FavoritesStore`. A favorites-list screen (`:feature:favorites`, opened from the FAB menu in `:integration`, fetching each saved id from the GitHub API and reusing `core:designsystem`'s `GithubRepositoryList`) was added afterwards; a favorite-sourced detail entry point that reads the base repo locally remains deferred.
7. **iOS-specific polish** — safe area handling, native share sheet expect/actual, Nav3 iOS edge-gesture tuning, Xcode app icon/launch screen.
8. **Stretch: desktop target** — sanity check that the umbrella + core layers are genuinely platform-agnostic.

---

## 6. Ktor client + interceptors (`core:network`)

### 6.1 Platform engine via expect/actual

```kotlin
// core/network/src/commonMain/kotlin/HttpClientEngine.kt
expect fun provideHttpClientEngine(): HttpClientEngine
```

```kotlin
// core/network/src/androidMain/kotlin/HttpClientEngine.kt
actual fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create()
```

```kotlin
// core/network/src/iosMain/kotlin/HttpClientEngine.kt
actual fun provideHttpClientEngine(): HttpClientEngine = Darwin.create()
```

### 6.2 AuthTokenProvider abstraction

```kotlin
// core/network/src/commonMain/kotlin/AuthTokenProvider.kt
interface AuthTokenProvider {
    fun getToken(): String?
}
```

### 6.3 Base client factory (generic, no domain models)

```kotlin
// core/network/src/commonMain/kotlin/GitHubHttpClient.kt
fun createGitHubHttpClient(
    tokenProvider: AuthTokenProvider
): HttpClient = HttpClient(provideHttpClientEngine()) {
    expectSuccess = true // makes Ktor throw ClientRequestException/ServerResponseException on
                          // 4xx/5xx instead of returning them as normal responses — required for
                          // safeApiCall (§6.4) to be able to catch them.

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    defaultRequest { url("https://api.github.com/") }

    // Interceptor 1: auth — attach PAT if present
    install(createClientPlugin("AuthInterceptor") {
        onRequest { request, _ ->
            tokenProvider.getToken()?.let { token ->
                request.headers.append(HttpHeaders.Authorization, "token $token")
            }
        }
    })

    // Interceptor 2 (optional): logging
    install(Logging) { level = LogLevel.INFO }
}
```

### 6.4 Error handling: `ApiError` + `safeApiCall`

`core:network` exposes no per-endpoint request helpers and no domain models — every feature's remote data sources call the injected `HttpClient` directly (§6.5). What `core:network` *does* own is the single place HTTP failures get translated into `Result`:

```kotlin
// core/network/src/commonMain/kotlin/HttpResponseResult.kt
sealed class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class Network(val cause0: Throwable) : ApiError("Network error", cause0)
    data class Timeout(val cause0: Throwable) : ApiError("Request timed out", cause0)
    data class Http(val code: Int, val body: String?) : ApiError("HTTP $code")
    data class Serialization(val cause0: Throwable) : ApiError("Failed to parse response", cause0)
    data class Unknown(val cause0: Throwable) : ApiError("Unexpected error", cause0)
}

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: ClientRequestException) { // 4xx
        Result.failure(ApiError.Http(e.response.status.value, e.response.bodyAsTextSafe()))
    } catch (e: ServerResponseException) { // 5xx
        Result.failure(ApiError.Http(e.response.status.value, e.response.bodyAsTextSafe()))
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiError.Timeout(e))
    } catch (e: IOException) { // no internet, DNS failure, etc.
        Result.failure(ApiError.Network(e))
    } catch (e: SerializationException) {
        Result.failure(ApiError.Serialization(e))
    } catch (e: CancellationException) {
        throw e // NEVER swallow this — breaks structured concurrency
    } catch (e: Exception) {
        Result.failure(ApiError.Unknown(e))
    }
}
```

This relies on `expectSuccess = true` (§6.3) so a non-2xx response actually throws `ClientRequestException`/`ServerResponseException` instead of silently coming back as a normal response.

### 6.5 Remote data sources are the only thing that calls Ktor directly

Once a feature's `data` layer grows past a couple of files, split it into three subpackages rather than leaving everything flat in `data/`:

- **`data/models/`** — wire-format DTOs, `@Serializable`, feature-owned and `internal` (a GitHub-shaped response, per §4.5, stays feature-owned even though its *shape* — items + pagination — looks generic; pagination itself isn't re-wrapped in a DTO-layer type here — see below). The visibility rule: **DTOs in feature modules are `internal`; DTOs in `:shared:*` modules are `public`**, because their whole purpose is to be used from other modules (the shared `GithubRepositoryDto` is the current example, referenced from the response wrappers below). Every type here is `Dto`-suffixed for consistency. Fields are camelCase; where GitHub's wire JSON key doesn't match (it's snake_case), annotate the field with `@SerialName("wire_name")` on its own line directly above the field. One file per group of *related* declarations — a response wrapper plus the item type(s) nested inside it, e.g. a response wrapper plus any item types only that wrapper uses live together, while unrelated DTO groups (e.g. user search vs. repo search) get their own file.
- **`data/datasources/`** — the data-source classes themselves (`XyzRemoteDataSource`, later `XyzLocalDataSource`), plus any helper functions used exclusively by them — request-building helpers (e.g. query-qualifier or sort-param builders) and response-parsing helpers (e.g. a `Headers.hasNextPage()` extension, in its own `PaginationUtil.kt`) both live here, next to the class(es) that use them, not in `models/`. A generic type and a standalone extension function that happen to serve a related purpose still get separate files if they're not the same declaration.
- **`data/repositories/`** — the repository class(es).

A DTO for an endpoint that has a snake_case wire format looks like this:

```kotlin
// feature/search/src/commonMain/kotlin/data/models/GithubRepositorySearchResponseDto.kt
@Serializable
internal data class GithubRepositorySearchResponseDto(
    val items: List<GithubRepositoryDto> = emptyList(),
)

// shared/github-api/src/commonMain/kotlin/GithubRepositoryDto.kt — public: shared by several features
@Serializable
data class GithubRepositoryDto(
    val id: Long,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val owner: GithubRepositoryOwnerDto,
    val description: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int = 0,
    @SerialName("forks_count")
    val forksCount: Int = 0,
    val language: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
)

@Serializable
data class GithubRepositoryOwnerDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
```

Every remote data source function wraps its Ktor call in `safeApiCall`, turns the raw response into a DTO, and maps that DTO into the `entities` type it returns — the request shape (path, query params, headers, method) *and* the DTO→entity mapping both live here; `core:network` has no opinion on either, and the DTO type never crosses back out of the data source. When the endpoint is paginated, the data source builds the `PagedResult` itself (imported from `:core:common/entities` — §2.2) directly around the mapped items; there's no separate DTO-layer pagination wrapper to construct first, since `PagedResult` already carries no domain opinion and is exactly what the function's `Result<T>` should wrap:

```kotlin
// feature/search/src/commonMain/kotlin/data/datasources/GithubRepositorySearchRemoteDataSource.kt
internal class GithubRepositorySearchRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun search(
        query: String,
        sort: SearchSort,
        language: String?,
        page: Int,
    ): Result<PagedResult<GithubRepository>> = safeApiCall {
        val params = buildMap {
            put("q", buildRepoSearchQualifiers(query, language))
            put("page", page.toString())
            putAll(sort.toSortParams())
        }
        val response = httpClient.get("search/repositories") {
            params.forEach { (k, v) -> parameter(key = k, value = v) }
        }
        val dto: GithubRepositorySearchResponseDto = response.body()

        PagedResult(
            items = dto.items.map { it.toGithubRepository() },
            hasNextPage = response.headers.hasNextPage(),
        )
    }
}
```

`PagedResult` is imported from `com.test.kmp.octoboard.core.common.entities`, and `GithubRepository` from `com.test.kmp.octoboard.shared.entities`. `hasNextPage()` and the `toGithubRepository()` mapper come from `:shared:github-api` (`com.test.kmp.octoboard.shared.githubapi`), together with the shared `GithubRepositoryDto`; `GithubRepositorySearchResponseDto` is imported from `data/models/GithubRepositorySearchResponseDto.kt`. A mapper that only one data source needs would instead stay `private` in that data source's file, as a short `=`-bodied extension function (see the code-style note below). `search` itself is also `=`-bodied even though it isn't a one-liner: a data source function whose entire body is a single `safeApiCall { ... }` wrapper is the other accepted exception — the `safeApiCall` call itself reads as the function's one real statement, so the block-body rule doesn't add anything here.

The repository never sees `HttpClient`, any Ktor type, or a DTO — its data source(s) already return `entities` types (here, an already-paginated `PagedResult<GithubRepository>`), so a repository function can be a straight pass-through:

```kotlin
// feature/search/src/commonMain/kotlin/data/repositories/SearchRepository.kt
class SearchRepository(
    private val githubRepositorySearchRemoteDataSource: GithubRepositorySearchRemoteDataSource,
    private val githubUserSearchRemoteDataSource: GithubUserSearchRemoteDataSource,
) {
    suspend fun searchRepositories(
        query: String,
        sort: SearchSort,
        language: String?,
        page: Int,
    ): Result<PagedResult<GithubRepository>> {
        return githubRepositorySearchRemoteDataSource.search(
            query = query,
            sort = sort,
            language = language,
            page = page,
        )
    }
}
```

Two code-style conventions this illustrates, applied project-wide: **use a block body (`{ ... return ... }`) rather than an expression body (`fun x(): T = ...`)**, reserving `=` for two accepted exceptions — a genuinely short one-liner utility (like a private DTO-to-entity mapper) and a function whose entire body is one `safeApiCall { ... }` wrapper (like `GithubRepositorySearchRemoteDataSource.search` above); every `data/repositories/` function is always block-bodied, since a repository's pass-through never qualifies as either exception. And: **prefer named arguments with a trailing comma after the last one** on any multi-parameter call — `searchRepositories` calling `githubRepositorySearchRemoteDataSource.search(...)` and `parameter(key = k, value = v)` above are both examples — it documents each argument at the call site and keeps future parameter reordering or additions a one-line diff.

A repository holding more than one data source, as above, is the norm — and becomes essential once local persistence arrives (§4.2's favorites toggle in `:feature:repodetail`, backed by `:shared:favorites-data`): it orchestrates a `XyzRemoteDataSource` and a local source (a `XyzLocalDataSource`, or a shared store such as `FavoritesStore`), never touching Ktor/SQLDelight/DTOs itself. Most repository functions still return a plain `Result<entities-type>` from a one-shot `suspend` call, exactly as above — but when a function needs to stay live against a local data source that changes over time (rather than just reading it once), it returns `Flow<Result<entities-type>>` instead, combining one one-shot remote fetch with a reactive local `Flow`. `GithubRepositoryDetailRepository.getRepositoryDetails` is the concrete example: it fetches the repo once from the network, then delegates into `FavoritesStore.observeIsFavorite(id): Flow<Boolean>`, so a later favorite toggle re-emits through the same `Flow<Result<GithubRepositoryDetail>>` without a second network call or a manual ViewModel state patch.

---

## 7. Navigation 3 (`core:ui-nav` + per-feature)

### 7.1 The contract each feature implements (`core:ui-nav`)

`core:ui-nav` defines the shape of a feature's navigation contribution — it does not know about any concrete `NavKey`.

```kotlin
// core/ui-nav/src/commonMain/kotlin/FeatureNavEntry.kt
interface FeatureNavEntry {
    val serializersModule: SerializersModule
    fun EntryProviderBuilder<NavKey>.registerEntries(navigator: OctoBoardNavigator)
}

interface OctoBoardNavigator {
    fun navigate(key: NavKey)
    fun back()
}
```

### 7.2 A feature's own NavKeys + entries (`:feature:search`)

NavKeys and entry registrations live in the feature's `navigation` package (§2.1), split **one destination per file** so the package doesn't turn into a single ever-growing file as a feature gains more destinations:

- **`<Feature>Key.kt`** — the feature's sealed `NavKey` interface, and *nothing else*. No nested destination keys. This is what makes per-destination files possible: Kotlin's sealed-subtype rule only requires same package + module, not same file, so each destination's key can live in its own file instead of being nested here.
- **One file per destination, `<Destination>NavEntry.kt`** — that destination's key type (`<Destination>NavEntryKey`, a **top-level** type implementing the feature's sealed interface — it can't be nested under `<Feature>Key`, since a nested type can only be declared inside its enclosing type's own file) plus an `EntryProviderScope<NavKey>.<destination>NavEntry(navigator: OctoBoardNavigator)` extension function containing that one destination's `entry<...> { ... }` registration. This is also where the screen's ViewModel is resolved from Koin — `ui`-layer composables never do this themselves.
- **`<Feature>NavEntries.kt`** — the feature's single `FeatureNavEntry` implementation. Its `serializersModule` lists every destination key's `::class` in one `polymorphic {}` block (centralized here rather than spread per-file, so the full set of a feature's destinations is visible in one place), and `registerEntries` just calls each destination's extension function. This is the only type in the `navigation` package that `:integration` references directly, so it — and every destination key it registers — **must be `public`, not `internal`**: they're consumed across the `:feature:*` → `:integration` module boundary, and `internal` fails to compile there.

```kotlin
// feature/search/src/commonMain/kotlin/navigation/SearchKey.kt
@Serializable
sealed interface SearchKey : NavKey
```

```kotlin
// feature/search/src/commonMain/kotlin/navigation/SearchNavEntry.kt
@Serializable
data object SearchNavEntryKey : SearchKey

fun EntryProviderScope<NavKey>.searchNavEntry(navigator: OctoBoardNavigator) {
    entry<SearchNavEntryKey>(metadata = ListDetailSceneStrategy.listPane()) {
        val viewModel = koinViewModel<SearchViewModel>()
        SearchScreen(
            viewModel = viewModel,
            onRepoSelected = { owner, repo ->
                navigator.navigate(GithubRepositoryDetailKey.Detail(owner, repo))
            }
        )
    }
}
```

```kotlin
// feature/search/src/commonMain/kotlin/navigation/SearchNavEntries.kt
object SearchNavEntries : FeatureNavEntry {
    override val serializersModule = SerializersModule {
        polymorphic(NavKey::class) { subclass(SearchNavEntryKey::class) }
    }

    override fun EntryProviderScope<NavKey>.registerEntries(navigator: OctoBoardNavigator) {
        searchNavEntry(navigator)
    }
}
```

This is the pattern every feature module follows once it has more than one destination: `:feature:repodetail` would get `GithubRepositoryDetailKey.kt`, one `<Destination>NavEntry.kt` per destination (e.g. `GithubRepositoryDetailNavEntry.kt`, and later a `ContributorsNavEntry.kt` if contributors get their own destination), and a `GithubRepositoryDetailNavEntries.kt` aggregator — all flat in `navigation/`, no subpackage needed at this scale.

`GithubRepositoryDetailKey` is defined in `:feature:repodetail`. `:feature:search` depends on `:feature:repodetail`'s **NavKey only** — cross-feature UI/ViewModel/DTO imports are still off-limits. If the `NavKey` needs to live somewhere neither feature depends on the other for, put minimal cross-referenced keys in `core:ui-nav` instead — but default to each feature owning its own keys, since one feature depending on another's `NavKey` type is normal and doesn't violate the "no feature depends on another feature's internals" rule.

### 7.3 Aggregation in `:integration`

This is the only place all `FeatureNavEntry` implementations are known together — it lives in `:integration`'s own `navigation` package (§2.1).

```kotlin
// integration/src/commonMain/kotlin/navigation/AppNavigation.kt
// rememberOctoBoardFeatureNavEntries() returns this list (remembered):
val featureNavEntries: List<FeatureNavEntry> = listOf(
    SearchNavEntries,
    GithubRepositoryDetailNavEntries,
    UserProfileNavEntries,
    AuthNavEntries
)

// Created once in IntegrationRoot (not inside the NavDisplay host) so other app-level UI, e.g. the
// FAB menu, can drive navigation. :core:ui-nav provides rememberOctoBoardNavigator(), which builds
// the back stack from the combined serializers and returns an OctoBoardNavController.
@Composable
fun IntegrationRoot() {
    val featureNavEntries = rememberOctoBoardFeatureNavEntries() // remembered, referentially stable
    val navigator = rememberOctoBoardNavigator(
        startDestination = SearchNavEntryKey,
        featureNavEntries = featureNavEntries,
    )
    OctoBoardNavHost(navigator = navigator, featureNavEntries = featureNavEntries)
    // ...FAB menu calls navigator.navigate(...)
}

@Composable
fun OctoBoardNavHost(
    navigator: OctoBoardNavController,
    featureNavEntries: List<FeatureNavEntry>,
) {
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    // FeatureNavDisplay wraps NavDisplay over navigator's back stack and registers every entry.
    FeatureNavDisplay(
        navigator = navigator,
        featureNavEntries = featureNavEntries,
        sceneStrategies = listOf(listDetailStrategy),
    )
}
```

### 7.4 Notes for the AI implementing this

- Always pass `configuration` (with the combined `SerializersModule`) to `rememberNavBackStack` — the reflection-only overload will crash on iOS at runtime.
- `ListDetailSceneStrategy.listPane()` / `.detailPane()` metadata determine which entries render in which pane; entries without either fall back to single-pane behavior.
- For the contributors "extra" pane, add `SupportingPaneSceneStrategy` alongside `ListDetailSceneStrategy` in the `:integration` `NavDisplay` setup — feature entries just add the corresponding metadata.
- Verify back gesture behavior on both platforms after this phase: Android predictive back, iOS edge swipe (`EndEdgePanGestureBehavior`, tuned in the iOS-polish roadmap phase).

---

## 8. Koin wiring

### 8.0 Always type Koin declarations

Every `single`/`factory` call must specify its bound type via `<>`, and every `get()` call inside it must specify the type it's resolving — even though Koin can often infer both. This makes each module readable top-to-bottom without needing to open the class it wires: `single<GithubRepositorySearchRemoteDataSource> { GithubRepositorySearchRemoteDataSource(httpClient = get<HttpClient>()) }`, never bare `single { GithubRepositorySearchRemoteDataSource(get()) }`. Applies to every Koin module in the project, `core:*` and `:feature:*` alike.

### 8.1 A feature's own Koin module

```kotlin
// feature/search/src/commonMain/kotlin/di/SearchKoinModule.kt
val searchKoinModule = module {
    includes(networkKoinModule)
    single<GithubRepositorySearchRemoteDataSource> { GithubRepositorySearchRemoteDataSource(httpClient = get<HttpClient>()) }
    single<GithubUserSearchRemoteDataSource> { GithubUserSearchRemoteDataSource(httpClient = get<HttpClient>()) }
    single<SearchRepository> {
        SearchRepository(
            githubRepositorySearchRemoteDataSource = get<GithubRepositorySearchRemoteDataSource>(),
            githubUserSearchRemoteDataSource = get<GithubUserSearchRemoteDataSource>(),
        )
    }
    factory<SearchViewModel> { SearchViewModel(repository = get<SearchRepository>()) }
}
```

Every feature follows this pattern — the module lives inside the feature's own `di` package (§2.1), next to what it wires. Data sources, repository, and ViewModel are otherwise plain constructor-injected classes with no Koin awareness of their own.

**A Koin module declares the modules it depends on with `includes(...)`** — `searchKoinModule` includes `networkKoinModule` for `HttpClient`, and the repo detail and favorites modules also include `favoritesDataKoinModule` (from `:shared:favorites-data`) for `FavoritesStore`. This keeps a feature self-describing: `:integration` cannot forget a module the feature needs (a missing one only fails at runtime, when the dependency is first resolved), and Koin loads a module included from several places only once. Bindings that only the app can supply (§8.4) are not included; they are the one thing `:integration` still has to provide.

### 8.2 core:network's Koin module

`core:network` has no navigation/ui/presentation/entities (§2.1's scope note) — just this `di` package.

```kotlin
// core/network/src/commonMain/kotlin/di/NetworkKoinModule.kt
val networkKoinModule = module {
    single<HttpClient> { createGitHubHttpClient(tokenProvider = get<AuthTokenProvider>()) }
}
```

Note `get<AuthTokenProvider>()` resolves the interface — the concrete binding happens in `:integration`, not here.

### 8.3 Aggregation in `:integration`

Lives in `:integration`'s own `di` package (§2.1).

```kotlin
// integration/src/commonMain/kotlin/di/AppModules.kt
val commonAppModules: List<Module> = listOf(
    searchKoinModule,
    githubRepositoryDetailKoinModule,
    userProfileKoinModule,
    authKoinModule,
    module {
        single<AuthTokenProvider> { get<SecureStorage>() }
    }
)
```

Shared modules such as `networkKoinModule` and `favoritesDataKoinModule` are not listed here: the features that need them `includes(...)` them (§8.1). Only the feature modules, the platform module (§8.4) and the app-level `AuthTokenProvider` binding are.

### 8.4 Platform-specific providers, still expect/actual at the edges

`SecureStorage` (in `:feature:auth`) and `DatabaseDriverFactory` (in `core:database`) are the two things that need a platform-specific implementation — declared as `expect class` in `commonMain`, with the platform module supplying the `actual` at app-init time.

```kotlin
// core/database/src/commonMain/kotlin/DatabaseDriverFactory.kt
expect class DatabaseDriverFactory {
    fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, dbName: String): SqlDriver
}
```

```kotlin
// androidApp platform module
val androidPlatformModule = module {
    single<DatabaseDriverFactory> { DatabaseDriverFactory(androidContext()) }
    single<SecureStorage> { SecureStorage(androidContext()) }
}
```

```kotlin
// iosApp platform module
val iosPlatformModule = module {
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }
    single<SecureStorage> { SecureStorage() }
}
```

### 8.5 Init calls

```kotlin
// androidApp/src/main/kotlin/OctoBoardApp.kt
class OctoBoardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OctoBoardApp)
            modules(commonAppModules + androidPlatformModule)
        }
    }
}
```

```kotlin
// integration/src/iosMain/kotlin/MainViewController.kt
fun MainViewController() = ComposeUIViewController {
    KoinApplication(koinConfiguration {
        modules(commonAppModules + iosPlatformModule)
    }) {
        OctoBoardNavHost()
    }
}
```

This file lives in `:integration` (the module that produces the iOS framework `iosApp` links against), not in `iosApp` itself — `iosApp` is a pure-Swift Xcode project with no Kotlin sources of its own.

---

## 9. Acceptance criteria per phase

- **Phase 1 (Skeleton):** app builds and runs on Android emulator and iOS simulator; one GitHub API call via the shared `HttpClient` renders as text on both; empty `:integration` module compiles and is consumed by both apps.
- **Phase 2 (Search):** `:feature:search` is fully self-contained (own UI, VM, DTOs, repo, NavKey, Koin module); `:integration` aggregates exactly one feature and both apps run it.
- **Phase 3 (Nav3 + adaptive):** tapping a search result navigates into `:feature:repodetail` via `NavKey` only; on a wide window (tablet/iPad), list and detail render side by side; on a phone/compact iPhone, navigation is single-pane.
- **Phase 4 (User profile):** navigating search → repo detail → contributor → user profile works and the back stack unwinds correctly on both platforms, with each hop crossing a feature boundary only through a `NavKey`; on a wide window (tablet/iPad), user profile renders in the same detail pane as repo detail, side by side with the search list — not as a full-pane destination — matching phase 3's list-detail behavior.
- **Phase 5 (Auth):** entering a PAT persists it via `:feature:auth`'s `SecureStorage`; `core:network`'s interceptor picks it up only through the `AuthTokenProvider` interface bound in `:integration`.
- **Phase 6 (Favorites):** tapping the star inline in the repo detail screen's header toggles favorite state, and it persists across app restarts using `:feature:repodetail`'s own minimal id-only SQLDelight schema, built on `core:database`'s driver factory. A favorites-list screen (FAB menu in `:integration`, repos fetched by id, list shared with search via `core:designsystem`) was added afterwards; a favorite-sourced detail entry point is still deferred.
- **Phase 7 (iOS polish):** safe-area insets correct on notched devices; share sheet opens natively; back-edge gesture feels native.
