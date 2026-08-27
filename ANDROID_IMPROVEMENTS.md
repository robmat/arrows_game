# Android App Improvements

Recommendations based on [Now in Android](https://github.com/android/nowinandroid) reference architecture,
its [Architecture Learning Journey](https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md),
[Modularization Learning Journey](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md),
and the [Material 3 Design Kit (Figma)](https://goo.gle/nia-figma).

---

## Current State

The project already has solid foundations:

| Area               | Status      | Notes                                                        |
|--------------------|-------------|--------------------------------------------------------------|
| Module structure   | Good        | `app`, `core:*`, `domain`, `data`, `feature:*`, `navigation` |
| Data layer         | Excellent   | Room + DataStore, interface-based repositories                |
| Navigation         | Strong      | Appyx with typed routing and custom transitions               |
| Testing            | Good        | JUnit + Mockito + fake doubles, Jacoco coverage               |
| Design system      | Basic       | Material 3 theme, shared colors/typography in `:core:ui`      |
| Lint & Detekt      | Minimal     | Detekt integrated but sparse config                           |
| DI                 | Manual only | `SkeinApplication` wires everything by hand                  |
| Build conventions  | None        | No `build-logic` module; boilerplate in each `build.gradle`   |
| Baseline profiles  | None        | No startup optimization                                       |
| Benchmarks         | None        | No Macrobenchmark module                                      |

---

## 1. Dependency Injection with Hilt

**Priority: High** | **Impact: Architecture, Testability**

NIA uses Hilt throughout. The project currently passes dependencies manually through
`SkeinApplication` and composable parameters. This creates tight coupling and makes testing
harder.

### What to do

- Add `hilt-android` + `hilt-compiler` (KSP) to the version catalog.
- Annotate `SkeinApplication` with `@HiltAndroidApp`.
- Annotate `MainActivity` with `@AndroidEntryPoint`.
- Convert `GameEngine` to `@HiltViewModel` with `@Inject constructor`.
- Create `@Module` classes in `:data` to `@Binds` repository interfaces to implementations.
- Provide `Room` database and DAOs via `@Singleton @Provides`.

### Example

```kotlin
// data/di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepository
    ): IUserPreferencesRepository
}

// feature/game/GameEngine.kt
@HiltViewModel
class GameEngine @Inject constructor(
    private val repository: IUserPreferencesRepository,
    private val gameStateDao: GameStateDao,
) : ViewModel()
```

### Testing benefit

Hilt provides `@TestInstallIn` to swap real modules for test doubles without
touching production code. This eliminates the manual fake-wiring in test setup.

---

## 2. Build Logic Convention Plugins

**Priority: High** | **Impact: Build maintainability, DRY**

NIA centralizes all Gradle configuration in a `build-logic/` included build with convention
plugins (`AndroidLibraryConventionPlugin`, `AndroidHiltConventionPlugin`, etc.). The project
currently duplicates `compileSdk`, `minSdk`, Compose compiler settings, and test config across
every module.

### What to do

- Create `build-logic/convention/` as an included build.
- Define convention plugins:
  - `arrows.android.library` — shared `compileSdk`, `minSdk`, Kotlin options.
  - `arrows.android.library.compose` — Compose compiler + Material 3 dependencies.
  - `arrows.android.hilt` — Hilt + KSP setup.
  - `arrows.android.feature` — combines library + compose + hilt for feature modules.
  - `arrows.detekt` — shared Detekt configuration.
- Replace per-module boilerplate with `plugins { id("arrows.android.feature") }`.

### Result

Adding a new feature module goes from ~40 lines of Gradle config to:

```kotlin
plugins {
    id("arrows.android.feature")
}

dependencies {
    implementation(projects.core.models)
    implementation(projects.core.ui)
}
```

---

## 3. Baseline Profiles & Startup Optimization

**Priority: High** | **Impact: App startup time, Runtime performance**

NIA uses baseline profiles to enable AOT compilation of critical startup paths.
The project has zero startup optimization.

### What to do

- Add a `:benchmark` module using `com.android.test` plugin.
- Add a `:baselineprofile` module using `androidx.baselineprofile` plugin.
- Write a `BaselineProfileGenerator` that exercises:
  - App cold start → main menu render.
  - Navigation to game screen → board render.
  - Navigation to settings.
- Generate profiles with `./gradlew :app:generateBaselineProfile`.
- Profiles land in `app/src/main/generated/baselineProfiles/baseline-prof.txt`.
- Gradle automatically bundles them into the release APK.

### Expected improvement

Baseline profiles typically yield 20-40% faster cold start and smoother initial frame rendering,
since Compose composables and drawing code are pre-compiled.

---

## 4. Enhanced Design System

**Priority: Medium** | **Impact: UI consistency, Developer velocity**

NIA has a dedicated `:core:designsystem` module with a component catalog app (`app-nia-catalog`).
The project's `:core:ui` has theme files and a few shared components but no formal system.

### What to do

- Rename/reorganize `:core:ui` to act as the single design system source.
- Formalize design tokens aligned with Material 3:
  - **Color**: Export from [Material Theme Builder](https://m3.material.io/theme-builder) into
    `Color.kt` with semantic names (`onSurface`, `primaryContainer`, etc.).
  - **Typography**: Define a `Type.kt` with `bodyLarge`, `titleMedium`, etc. using
    `MaterialTheme.typography`.
  - **Shape**: Define corner radius tokens (`ShapeDefaults`).
  - **Spacing**: Add a `Dimensions` object with standard padding values.
- Create an `SkeinTheme` composable wrapping `MaterialTheme` with the game's custom tokens.
- Add dynamic color support (`dynamicDarkColorScheme` / `dynamicLightColorScheme` on Android 12+).
- Optionally create an `app-catalog` module (like NIA) to preview all components in isolation.

### Component candidates

| Component          | Current location        | Improvement                              |
|--------------------|------------------------|------------------------------------------|
| `ShapeCard`        | `GeneratorView`        | Move to design system, make generic      |
| `SettingsSection`  | `SettingsView`         | Already reused; formalize as token-aware  |
| `SettingsToggle`   | `SettingsView`         | Move to design system                    |
| `ThemeButton`      | Inline in settings     | Generalize as `ColorSwatchPicker`        |
| `GameTopBar`       | `feature:game`         | Extract common `SkeinTopBar` to `:core:ui` |

---

## 5. Unidirectional Data Flow & UI State Modeling

**Priority: Medium** | **Impact: Architecture clarity, Testability**

NIA models every screen's state as a sealed interface hierarchy (`Loading`, `Success`, `Error`)
and transforms repository flows into a single `StateFlow<UiState>` via `stateIn()`.
The project's `GameEngine` exposes ~15 individual `mutableStateOf` properties instead.

### What to do

- Define sealed UI state per screen:

```kotlin
sealed interface GameUiState {
    data object Loading : GameUiState
    data class Playing(
        val level: GameLevel,
        val lives: Int,
        val maxLives: Int,
        val totalSnakes: Int,
        val entryProgress: Map<Int, Float>,
        val removalProgress: Map<Int, Float>,
        val flashingSnakeId: Int?,
        val isEntryAnimating: Boolean,
    ) : GameUiState
    data object Won : GameUiState
    data class GameOver(val canWatchAd: Boolean) : GameUiState
}
```

- Expose a single `val uiState: StateFlow<GameUiState>` from the ViewModel.
- Composables switch on the sealed type, guaranteeing all states are handled.

### Benefit

This makes it impossible for the UI to render an inconsistent combination of properties
(e.g., `isGameWon = true` while `lives = 0`) and simplifies screenshot testing since each
state variant is a single immutable snapshot.

---

## 6. Domain Layer Use Cases

**Priority: Medium** | **Impact: Separation of concerns, Reusability**

NIA places cross-repository logic in single-purpose use case classes with an `operator fun invoke()`.
The project's `:domain` module contains the game generator and models but no formal use cases.

### What to do

- Extract complex operations into use case classes:
  - `GenerateLevelUseCase` — wraps `GameGenerator` + shape provider + difficulty config.
  - `GetCurrentGameUseCase` — combines saved game state + user preferences into a playable level.
  - `AdvanceLevelUseCase` — increments level, calculates new difficulty, saves state.
- Use cases receive repository interfaces via constructor injection (Hilt).
- ViewModels call use cases instead of repositories directly.

### Example

```kotlin
class GenerateLevelUseCase @Inject constructor(
    private val gameGenerator: GameGenerator,
    private val shapeProvider: BoardShapeProvider,
    private val prefsRepository: IUserPreferencesRepository,
) {
    suspend operator fun invoke(
        width: Int, height: Int, shapeName: String?,
        onProgress: (Float) -> Unit,
    ): GameLevel { /* ... */ }
}
```

---

## 7. Testing Improvements

**Priority: Medium** | **Impact: Test reliability, Maintenance**

NIA avoids mocking libraries entirely, using hand-written test doubles that implement production
interfaces. The project uses Mockito alongside fake doubles.

### What to do

- **Consolidate on fakes over mocks**: The project already has `FakeUserPreferencesRepository`
  and `FakeGameStateDao`. Extend this pattern to cover all repositories and data sources.
  Remove Mockito dependency over time.
- **Create `:core:testing` module**: Move shared test utilities, fakes, and test fixtures into a
  dedicated module (NIA pattern). Currently fakes are duplicated across test source sets.
- **Add screenshot tests**: Use [Roborazzi](https://github.com/takahirom/roborazzi) (NIA's choice)
  for composable screenshot regression testing. Critical screens: game board, main menu, settings.
- **Add UI tests for navigation**: Verify Appyx back-stack transitions between screens using
  `appyx-testing-junit4` (already in dependencies but underutilized).

### Shared testing module structure

```
core/testing/
  src/main/
    FakeUserPreferencesRepository.kt
    FakeGameStateDao.kt
    TestGameLevelFactory.kt    // Builders for test levels
    MainDispatcherRule.kt       // Coroutine test rule
```

---

## 8. Detekt & Lint Hardening

**Priority: Low** | **Impact: Code quality enforcement**

The current Detekt config only ignores `@Composable` naming. NIA enforces architecture rules
through custom lint checks.

### What to do

- **Expand Detekt rules** in `config/detekt/detekt.yml`:
  - `complexity.LongMethod` — catch oversized composables.
  - `complexity.LongParameterList` — flag functions with >6 params (refactor to data classes).
  - `style.MagicNumber` — enforce use of `GameConstants` instead of inline numbers.
  - `style.MaxLineLength` — consistency across the codebase.
  - `style.UnusedImports` — automatic cleanup.
- **Add architecture lint rules** (optional `:lint` module):
  - Feature modules must not import from other feature `impl` modules.
  - `:core:*` must not depend on `:feature:*`.
  - `:domain` must not depend on Android framework classes.
- **Enable `warningsAsErrors`** in Kotlin compiler options for release builds.

---

## 9. Offline-First Data Sync Pattern

**Priority: Low** | **Impact: Future-proofing for cloud sync**

NIA uses WorkManager with `SyncWorker` and exponential backoff for data synchronization.
The project is currently fully local, but if cloud save/leaderboards are ever added, this
pattern becomes essential.

### What to prepare

- Define a `Synchronizer` interface in `:data`:

```kotlin
interface Synchronizer {
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}
```

- Implement `Syncable` on repositories that could be backed by remote data.
- When ready, add a `SyncWorker` (WorkManager `CoroutineWorker`) that calls
  `repository.syncWith()` for each syncable repository.

This is a low-priority preparation step — only implement when cloud features are planned.

---

## 10. Compose Compiler Metrics & Reports

**Priority: Low** | **Impact: Performance debugging**

NIA provides a Gradle flag to generate Compose stability reports.

### What to do

- Add to root `build.gradle.kts`:

```kotlin
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            if (project.findProperty("enableComposeCompilerMetrics") == "true") {
                freeCompilerArgs.addAll(
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                        layout.buildDirectory.dir("compose-metrics").get().asFile.absolutePath,
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                        layout.buildDirectory.dir("compose-reports").get().asFile.absolutePath,
                )
            }
        }
    }
}
```

- Run: `./gradlew assembleRelease -PenableComposeCompilerMetrics=true`
- Inspect reports in `build/compose-reports/` for unstable classes and skippable composables.
- Mark immutable data classes with `@Stable` or `@Immutable` where the compiler can't infer it.

---

## Implementation Order

| Phase | Items                                        | Effort   |
|-------|----------------------------------------------|----------|
| 1     | Build-logic convention plugins               | 1-2 days |
| 2     | Hilt dependency injection                    | 2-3 days |
| 3     | Baseline profiles + benchmark module         | 1 day    |
| 4     | Sealed UI state modeling                     | 1-2 days |
| 5     | `:core:testing` module + screenshot tests    | 1-2 days |
| 6     | Design system formalization                  | 1-2 days |
| 7     | Domain layer use cases                       | 1 day    |
| 8     | Detekt/Lint hardening                        | 0.5 day  |
| 9     | Compose compiler metrics                     | 0.5 day  |
| 10    | Offline-first prep (when needed)             | 1 day    |

---

## Sources

- [Now in Android — GitHub](https://github.com/android/nowinandroid)
- [Architecture Learning Journey](https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md)
- [Modularization Learning Journey](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)
- [Now in Android Design — Figma](https://goo.gle/nia-figma)
- [Material 3 Design Kit — Figma](https://www.figma.com/community/file/1035203688168086460/material-3-design-kit)
- [Material Design 3](https://m3.material.io/)
