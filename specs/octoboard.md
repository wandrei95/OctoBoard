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

`core:*` modules contain **only genuinely non-feature-specific infrastructure** — things with no domain opinion (HTTP plumbing, DB driver setup, generic reusable UI components, navigation scaffolding). A `core:*` module must never depend on a `:feature:*` module. A `:feature:*` module may depend on `core:*` modules, but never on another `:feature:*` module directly — cross-feature navigation happens through `NavKey`s, not direct imports.

An **umbrella module (`:integration`)** sits above the feature modules and below the platform apps. It is the only module (besides the platform apps) allowed to depend on every feature module. Its job is purely aggregation: it merges each feature's Koin module list, each feature's nav entries, and each feature's `NavKey` serializers into one Koin module list and one `NavDisplay` setup that the platform apps consume.

```
              :androidApp        :iosApp
                     \             /
                      :integration
                             |
      -----------------------------------------------
      |         |            |            |          |
:feature:   :feature:   :feature:    :feature:  :feature:
 search     repodetail  userprofile  favorites   auth
      |         |            |            |          |
      -----------------------------------------------
                             |
      -----------------------------------------------
      |            |             |              |
:core:network  :core:database  :core:designsystem  :core:ui-nav
```

### 2.1 Internal layering within a module

Every module that owns UI (every `:feature:*` module, plus `:integration`) is internally organized into six layers, each in its own package, with a strict one-way dependency chain:

```
di → navigation → ui → presentation → data → entities
```

A layer may only depend on layers to its right. Concretely:

- **`entities`** — plain domain models (POJOs). No serialization annotations, no framework imports (no Ktor, no SQLDelight, no Koin, no Compose). Feature-owned, per §4.6's duplication-over-coupling principle — not shared across features.
- **`data`** — talks to `core:network` / `core:database`. Owns wire-format DTOs (`@Serializable`, kept internal to this package) and repository classes. Every public function's input/output is an `entities` type, never a DTO — the mapping happens inside `data`. Repositories are concrete classes; no interface/impl split unless a second implementation genuinely exists.
- **`presentation`** — ViewModels and UI-state classes. A ViewModel takes its `data`-layer dependencies via plain constructor injection — it never resolves anything from Koin itself. Exposes state as `StateFlow<UiState>`.
- **`ui`** — Compose screens only. Takes its ViewModel directly as a parameter (reads `collectAsState()`, calls VM methods directly) plus plain navigation-callback lambdas (e.g. `onRepoSelected: (owner: String, repo: String) -> Unit`). No Koin import. No navigation logic beyond invoking the callbacks it's handed — the screen doesn't know what a callback does or where it leads.
- **`navigation`** — owns this module's `NavKey`s and its `FeatureNavEntry`/entry registration (see §7). This is where `koinViewModel()` (or `koinInject()`) is called to resolve a screen's ViewModel, and where the `ui`-layer composable is invoked with that ViewModel plus navigation lambdas wired to `OctoBoardNavigator`.
- **`di`** — this module's Koin module definition(s) only (see §8).

**Hard rule: `ui`, `presentation`, `data`, and `entities` must never import anything from `org.koin.*`.** Only `di` and `navigation` are Koin-aware. This keeps every layer below `navigation` trivially constructible and testable without a DI container.

**Scope:** `core:*` modules have no screens or nav destinations, so they only carry a `di` package (their Koin module, per §8.2's pattern) — everything else in a `core:*` module stays flat at its root package. The full six-layer split applies to `:feature:*` modules and to `:integration`.

**Platform composition roots are exempt.** `:androidApp`'s `Application`/`Activity` classes and `:integration`'s iOS `MainViewController.kt` (§8.5) are where the whole apparatus is bootstrapped — they legitimately call into both `di` (starting Koin) and `navigation` (invoking the root composable) in one place, and aren't required to live inside a layer package themselves.

---

## 3. Module structure

```
:core:network        - Ktor HttpClient factory, interceptors, generic GithubApiClient
                        (typed GET/POST helpers, no domain models), AuthTokenProvider
                        interface, ApiError, RateLimitInfo
:core:database         - SqlDriver factory (expect/actual) ONLY — no tables, no DTOs.
                        Any feature that needs local storage asks this module for a
                        driver and defines its own SQLDelight schema.
:core:designsystem     - shared Compose theme, typography, and generic reusable
                        components (buttons, cards, loading/error/empty states,
                        avatar, chips). NEVER a full screen.
:core:ui-nav           - Navigation 3 scaffolding: the NavDisplay host, adaptive scene
                        strategy setup, and the FeatureNavEntry contract every feature
                        implements. Contains no feature NavKeys itself.

:feature:search        - SearchScreen, SearchViewModel, Search DTOs, SearchRepository
                        (uses core GithubApiClient), Search NavKeys + entries,
                        searchKoinModule
:feature:repodetail    - RepoDetailScreen, RepoDetailViewModel, RepoDetail DTOs,
                        RepoDetailRepository, RepoDetail NavKeys + entries,
                        repoDetailKoinModule
:feature:userprofile   - same pattern
:feature:favorites     - FavoritesScreen, FavoritesViewModel, own SQLDelight schema/DAO
                        (built on core:database's driver factory), FavoritesRepository,
                        Favorites NavKeys + entries, favoritesKoinModule
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
- Owns `RepoSummary`/`UserSummary` DTOs (its own view of a repo/user — see §4.6 on shared-shape duplication)
- List pane in the adaptive list-detail scaffold; navigates to `RepoDetailKey` / `UserProfileKey` from other features

### 4.2 `:feature:repodetail`
- `GET /repos/{owner}/{repo}`, `GET /repos/{owner}/{repo}/languages`, `GET /repos/{owner}/{repo}/contributors`
- Owns its own `RepoDetails`, `LanguageBreakdown`, `Contributor` DTOs
- Detail pane in the adaptive list-detail scaffold; contributors optionally shown in a third "extra" pane on very wide layouts

### 4.3 `:feature:userprofile`
- `GET /users/{username}`, `GET /users/{username}/repos`
- Owns its own `UserDetails` DTO
- Reachable from search results and from contributor avatars in repo detail (via `NavKey`, not direct module dependency)

### 4.4 `:feature:favorites`
- Star/unstar a repo locally
- Owns its own SQLDelight schema (`favorites.sq`) and DAO, built on `core:database`'s driver factory
- Owns its own `FavoriteRepo` persisted model, mapped from whatever DTO the star action originated from
- Fully offline; list backed by a `Flow` from its own database

### 4.5 `:feature:auth`
- User enters a GitHub PAT
- Owns `SecureStorage` (`expect`/`actual`: EncryptedSharedPreferences/DataStore on Android, Keychain on iOS) and implements `AuthTokenProvider`
- Rate-limit banner UI lives here too, sourced from `RateLimitInfo` exposed by `core:network`

### 4.6 On duplicated domain shapes across features

Search results, favorites, and user-profile repo lists all represent "a GitHub repo," but each feature defines its **own** DTO shaped for its own screen (e.g. `SearchRepoSummary` vs `FavoriteRepo` vs `ProfileRepoItem`), mapped independently from the raw JSON. This is deliberate, not an oversight — it's the standard trade-off of self-contained feature modules (duplication over coupling), and it's worth experiencing directly as part of this learning project. If the duplication becomes painful later, that's the natural moment to extract a shared shape into `core:network` — but don't do it up front.

---

## 5. Roadmap (implement in this order)

1. **Skeleton** — KMP project, CMP targets for Android + iOS, `ComposeUIViewController` entry point, `core:network`'s `GithubApiClient` + platform engines, one smoke-test call rendered as raw text on both platforms, empty `:integration` module wired into both apps.
2. **Search, fully self-contained** — `:feature:search` with its own UI, ViewModel, DTOs, repository, NavKey, Koin module. `:integration` aggregates just this one feature's Koin module and nav entry to prove the umbrella pattern works end to end before adding more features.
3. **Navigation 3 + adaptive, cross-feature** — add `:feature:repodetail`, wire `SearchKey → RepoDetailKey` navigation through `:integration`'s combined `NavDisplay` and `SerializersModule`. Introduce `ListDetailSceneStrategy`.
4. **User profile** — add `:feature:userprofile`, cross-navigate from both search and repo detail (contributors).
5. **Auth + interceptors** — add `:feature:auth`, `AuthTokenProvider` interface in `core:network`, bind the concrete implementation in `:integration`, rate-limit interceptor + banner.
6. **Favorites + SQLDelight** — add `:feature:favorites` with its own schema built on `core:database`'s driver factory.
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
    tokenProvider: AuthTokenProvider,
    onRateLimitUpdate: (RateLimitInfo) -> Unit
): HttpClient = HttpClient(provideHttpClientEngine()) {
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

    // Interceptor 2: rate limit — read response headers, push to shared state
    install(createClientPlugin("RateLimitInterceptor") {
        onResponse { response ->
            val remaining = response.headers["X-RateLimit-Remaining"]?.toIntOrNull()
            val reset = response.headers["X-RateLimit-Reset"]?.toLongOrNull()
            if (remaining != null && reset != null) {
                onRateLimitUpdate(RateLimitInfo(remaining, reset))
            }
        }
    })

    // Interceptor 3 (optional): logging
    install(Logging) { level = LogLevel.INFO }
}
```

### 6.4 Generic API client — no domain models here

`core:network` exposes typed request helpers, but the response types are supplied by each feature, not defined in `core`:

```kotlin
// core/network/src/commonMain/kotlin/GithubApiClient.kt
class GithubApiClient(private val httpClient: HttpClient) {
    suspend inline fun <reified T> get(
        path: String,
        params: Map<String, String> = emptyMap()
    ): T = httpClient.get(path) {
        params.forEach { (k, v) -> parameter(k, v) }
    }.body()
}
```

Each feature's repository calls this with its own DTO type, e.g. inside `:feature:search`:

```kotlin
class SearchRepository(private val api: GithubApiClient) {
    suspend fun searchRepos(query: String): SearchResponse =
        api.get("search/repositories", mapOf("q" to query))
}
```

`SearchResponse` and everything inside it is defined in `:feature:search`, not in `core:network`.

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

NavKeys and entry registrations live in the feature's `navigation` package (§2.1). The entry registration is also where the screen's ViewModel is resolved from Koin — `ui`-layer composables never do this themselves.

```kotlin
// feature/search/src/commonMain/kotlin/navigation/SearchNavKeys.kt
@Serializable
sealed interface SearchKey : NavKey {
    @Serializable data object Search : SearchKey
}
```

```kotlin
// feature/search/src/commonMain/kotlin/navigation/SearchNavEntry.kt
object SearchNavEntry : FeatureNavEntry {
    override val serializersModule = SerializersModule {
        polymorphic(NavKey::class) { subclass(SearchKey.Search::class) }
    }

    override fun EntryProviderBuilder<NavKey>.registerEntries(navigator: OctoBoardNavigator) {
        entry<SearchKey.Search>(metadata = ListDetailSceneStrategy.listPane()) {
            val viewModel = koinViewModel<SearchViewModel>()
            SearchScreen(
                viewModel = viewModel,
                onRepoSelected = { owner, repo ->
                    navigator.navigate(RepoDetailKey.Detail(owner, repo))
                }
            )
        }
    }
}
```

`RepoDetailKey` is defined in `:feature:repodetail`. `:feature:search` depends on `:feature:repodetail`'s **NavKey only** — cross-feature UI/ViewModel/DTO imports are still off-limits. If the `NavKey` needs to live somewhere neither feature depends on the other for, put minimal cross-referenced keys in `core:ui-nav` instead — but default to each feature owning its own keys, since one feature depending on another's `NavKey` type is normal and doesn't violate the "no feature depends on another feature's internals" rule.

### 7.3 Aggregation in `:integration`

This is the only place all `FeatureNavEntry` implementations are known together — it lives in `:integration`'s own `navigation` package (§2.1).

```kotlin
// integration/src/commonMain/kotlin/navigation/AppNavigation.kt
val allFeatureNavEntries: List<FeatureNavEntry> = listOf(
    SearchNavEntry,
    RepoDetailNavEntry,
    UserProfileNavEntry,
    FavoritesNavEntry,
    AuthNavEntry
)

val combinedSerializersModule = SerializersModule {
    allFeatureNavEntries.forEach { include(it.serializersModule) }
}

val combinedSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = combinedSerializersModule
}

@Composable
fun OctoBoardNavHost() {
    val backStack = rememberNavBackStack(
        SearchKey.Search,
        configuration = combinedSavedStateConfiguration
    )
    val navigator = remember {
        object : OctoBoardNavigator {
            override fun navigate(key: NavKey) { backStack.add(key) }
            override fun back() { backStack.removeLastOrNull() }
        }
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.back() },
        sceneStrategy = listDetailStrategy,
        entryProvider = entryProvider {
            allFeatureNavEntries.forEach { with(it) { registerEntries(navigator) } }
        }
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

### 8.1 A feature's own Koin module

```kotlin
// feature/search/src/commonMain/kotlin/di/SearchKoinModule.kt
val searchKoinModule = module {
    single { SearchRepository(get()) }
    factory { SearchViewModel(get()) }
}
```

Every feature follows this pattern — the module lives inside the feature's own `di` package (§2.1), next to what it wires. `SearchRepository` and `SearchViewModel` are otherwise plain constructor-injected classes with no Koin awareness of their own.

### 8.2 core:network's Koin module

`core:network` has no navigation/ui/presentation/entities (§2.1's scope note) — just this `di` package.

```kotlin
// core/network/src/commonMain/kotlin/di/NetworkKoinModule.kt
val networkKoinModule = module {
    single { MutableStateFlow(RateLimitInfo.UNKNOWN) }
    single {
        createGitHubHttpClient(
            tokenProvider = get<AuthTokenProvider>(),
            onRateLimitUpdate = { get<MutableStateFlow<RateLimitInfo>>().value = it }
        )
    }
    single { GithubApiClient(get()) }
}
```

Note `get<AuthTokenProvider>()` resolves the interface — the concrete binding happens in `:integration`, not here.

### 8.3 Aggregation in `:integration`

Lives in `:integration`'s own `di` package (§2.1).

```kotlin
// integration/src/commonMain/kotlin/di/AppModules.kt
val commonAppModules: List<Module> = listOf(
    networkKoinModule,
    databaseKoinModule,
    searchKoinModule,
    repoDetailKoinModule,
    userProfileKoinModule,
    favoritesKoinModule,
    authKoinModule,
    module {
        single<AuthTokenProvider> { get<SecureStorage>() }
    }
)
```

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
    single { DatabaseDriverFactory(androidContext()) }
    single { SecureStorage(androidContext()) }
}
```

```kotlin
// iosApp platform module
val iosPlatformModule = module {
    single { DatabaseDriverFactory() }
    single { SecureStorage() }
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

- **Phase 1 (Skeleton):** app builds and runs on Android emulator and iOS simulator; one GitHub API call via `GithubApiClient` renders as text on both; empty `:integration` module compiles and is consumed by both apps.
- **Phase 2 (Search):** `:feature:search` is fully self-contained (own UI, VM, DTOs, repo, NavKey, Koin module); `:integration` aggregates exactly one feature and both apps run it.
- **Phase 3 (Nav3 + adaptive):** tapping a search result navigates into `:feature:repodetail` via `NavKey` only; on a wide window (tablet/iPad), list and detail render side by side; on a phone/compact iPhone, navigation is single-pane.
- **Phase 4 (User profile):** navigating search → repo detail → contributor → user profile works and the back stack unwinds correctly on both platforms, with each hop crossing a feature boundary only through a `NavKey`.
- **Phase 5 (Auth):** entering a PAT persists it via `:feature:auth`'s `SecureStorage`; `core:network`'s interceptor picks it up only through the `AuthTokenProvider` interface bound in `:integration`; rate-limit banner updates from live response headers.
- **Phase 6 (Favorites):** starring a repo persists across app restarts using `:feature:favorites`'s own SQLDelight schema, built on `core:database`'s driver factory.
- **Phase 7 (iOS polish):** safe-area insets correct on notched devices; share sheet opens natively; back-edge gesture feels native.
