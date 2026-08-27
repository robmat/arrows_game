# Modularization Plan — Skein Android (Clean Architecture)

## Goals

- Enforce strict layer boundaries (data cannot import UI, domain has no Android deps)
- Reduce incremental build times via Gradle module parallelism
- Enable feature-level isolation and independent testing
- Prepare for Hilt DI (replaces the current manual injection in `SkeinApplication`)
- Make the pure game engine reusable and testable without Android

---

## Proposed Module Graph

```
:app
 ├── :navigation
 ├── :feature:home
 ├── :feature:game
 ├── :feature:generate
 ├── :feature:settings
 ├── :data
 └── :ads

:navigation
 ├── :feature:home
 ├── :feature:game
 ├── :feature:generate
 └── :feature:settings

:feature:home
 ├── :domain
 ├── :core:ui
 └── :ads

:feature:game
 ├── :domain
 ├── :data
 ├── :core:ui
 └── :ads

:feature:generate
 ├── :domain
 └── :core:ui

:feature:settings
 ├── :domain
 ├── :core:ui
 └── :ads

:data
 └── :domain          ← implements repository interfaces defined in :domain

:ads
 └── :core:models

:domain
 └── :core:models

:core:ui
 └── :core:models

:core:models           ← pure Kotlin, zero Android deps
```

Dependency rule: **inner layers must never import outer layers**.
`core:models → domain → data/ads/features → navigation → app`

---

## Module Definitions

### `:core:models`
**Kind**: pure Kotlin (no Android, no Compose)
**Responsibility**: shared domain types and repository contracts used everywhere

Files to move here:
```
engine/GameModels.kt                  → Point, Direction, Snake, GameLevel
engine/GameGeneratorModels.kt         → GenerationResult, GeneratedSnake, …
engine/GenerationParams.kt
engine/GenerationCriterion.kt
engine/ParameterObjects.kt
engine/ExtraParameterObjects.kt
GameConstants.kt
```

New interfaces to define here:
```kotlin
interface IUserPreferencesRepository   // currently implemented in data/
interface IGameStateRepository         // abstraction over GameStateDao transactions
```

---

### `:domain`
**Kind**: pure Kotlin (JVM only — allows fast unit tests without Android emulator)
**Responsibility**: all game business logic and algorithms

Files to move here:
```
engine/GameGenerator.kt
engine/GenerationUtils.kt
engine/LevelProgression.kt
engine/SnakeBuilder.kt
engine/SolvabilityChecker.kt
engine/InputHandler.kt
engine/TapHandler.kt
engine/BoardShapeProvider.kt           ← interface only; impl stays in :data
engine/BoardImageAbstractions.kt       ← interfaces / pure models
```

Existing test files that already test this layer move unchanged:
```
test/engine/GameEngineTest*.kt
test/engine/GameGeneratorTest*.kt
test/engine/LevelProgressionTest.kt
test/engine/FakeGameStateDao.kt        ← test double, stays in :domain test sources
test/engine/FakeUserPreferencesRepository.kt
test/engine/JvmBoardImage.kt
```

---

### `:data`
**Kind**: Android library (Room, DataStore, Android resources)
**Responsibility**: all persistence — Room database, DAOs, repository implementations, migrations

Files to move here:
```
data/AppDatabase.kt
data/UserPreferencesEntity.kt
data/UserPreferencesDao.kt
data/UserPreferencesRepository.kt      ← implements IUserPreferencesRepository
data/GameBoardEntity.kt
data/SnakeEntity.kt
data/SnakeBodyPointEntity.kt
data/GameStateDao.kt
data/DataStoreMigration.kt
data/ShapeRegistry.kt
data/AndroidResourceBoardShapeProvider.kt  ← implements BoardShapeProvider
engine/AndroidBoardImage.kt                ← Android Bitmap wrapper, impl of interface
```

Gradle deps:
```kotlin
implementation(libs.room.runtime)
implementation(libs.room.ktx)
ksp(libs.room.compiler)
implementation(libs.datastore.preferences)
implementation(project(":domain"))
```

---

### `:ads`
**Kind**: Android library
**Responsibility**: all AdMob integration — consent, rewarded ads, interstitials

Files to move here:
```
ads/ConsentManager.kt
ads/RewardAdManager.kt
ads/InterstitialAdManager.kt
```

Expose a simple interface `:core:models` can reference (e.g., `AdState`) so features can observe ad status without importing `:ads` directly when they don't need to.

Gradle deps:
```kotlin
implementation(libs.play.services.ads)
implementation(libs.ump)
implementation(project(":core:models"))
```

---

### `:core:ui`
**Kind**: Android library (Compose)
**Responsibility**: design system — theme, colors, typography, shared UI primitives used by ≥2 features

Files to move here:
```
ui/theme/Color.kt
ui/theme/ThemeColors.kt
ui/theme/Type.kt
ui/AppNavigationBar.kt
ui/NavigationDestination.kt
ui/SettingsBaseComponents.kt
ui/SettingsUtils.kt
```

Gradle deps:
```kotlin
implementation(libs.compose.material3)
implementation(libs.compose.icons.extended)
implementation(project(":core:models"))
```

---

### `:feature:home`
**Kind**: Android library (Compose + ViewModel)
**Responsibility**: home screen, global app-level state (theme, user preferences observer)

Files to move here:
```
ui/AppViewModel.kt          ← rename or split; owns theme + preferences + level state
MainScreen (from MainActivity.kt)
navigation/HomeNode.kt
```

Note: `AppViewModel` currently owns too many concerns. Split it:
- `ThemeViewModel` (theme selection) → `:feature:home` or `:core:ui`
- Game level/lives state → `GameEngine` in `:feature:game`
- Ad state → `:ads` module via StateFlow

---

### `:feature:game`
**Kind**: Android library (Compose + ViewModel)
**Responsibility**: active game session — engine, rendering, input, animations, sound, ads integration

Files to move here:
```
engine/GameEngine.kt             ← ViewModel; depends on :domain logic
engine/LevelManager.kt
engine/RemovalAnimator.kt
engine/TransformationState.kt
engine/BoardImageProcessor.kt
SkeinGameView.kt
GameScreen.kt
SkeinBoardRenderer.kt
TapAnimationState.kt
SoundManager.kt
GameActivityHelpers.kt
ui/game/GameComponents.kt
ui/game/IntroOverlay.kt
ui/game/IntroState.kt
ui/game/WinCelebrationScreen.kt
ui/game/CelebrationContentSelector.kt
ui/ads/BannerAdView.kt
navigation/GameNode.kt
```

Gradle deps:
```kotlin
implementation(project(":domain"))
implementation(project(":data"))
implementation(project(":core:ui"))
implementation(project(":ads"))
```

---

### `:feature:generate`
**Kind**: Android library (Compose + ViewModel)
**Responsibility**: custom level generator screen

Files to move here:
```
GenerateScreen.kt
navigation/GenerateNode.kt
```

Gradle deps:
```kotlin
implementation(project(":domain"))
implementation(project(":core:ui"))
// Reuses GameEngine from :feature:game for the preview — consider a shared
// ViewModel factory or expose a lightweight preview composable from :feature:game
```

---

### `:feature:settings`
**Kind**: Android library (Compose + ViewModel)
**Responsibility**: settings screen — theme picker, sound/vibration, ad settings, licenses

Files to move here:
```
SettingsScreen.kt
navigation/SettingsNode.kt
ui/SettingsComponents.kt
ui/AdSettingsSection.kt
ui/AdSettingsSectionState.kt
ui/ThirdPartyLicensesDialog.kt
ui/DebugComponents.kt
```

Gradle deps:
```kotlin
implementation(project(":domain"))
implementation(project(":core:ui"))
implementation(project(":ads"))
```

---

### `:navigation`
**Kind**: Android library
**Responsibility**: Appyx navigation graph — routes, root node, transitions

Files to move here:
```
navigation/NavTarget.kt
navigation/RootNode.kt
navigation/transitions/NavTransitions.kt
navigation/transitions/NavTransitionType.kt
navigation/transitions/RandomTransitionHandler.kt
navigation/transitions/TransitionPicker.kt
```

Gradle deps:
```kotlin
implementation(libs.appyx.navigation)
implementation(project(":feature:home"))
implementation(project(":feature:game"))
implementation(project(":feature:generate"))
implementation(project(":feature:settings"))
```

---

### `:app`
**Kind**: Android application (thin shell)
**Responsibility**: `Application` class, `MainActivity`, DI wiring, manifest, ad unit IDs

Files that stay here:
```
SkeinApplication.kt    ← slimmed down; Hilt @HiltAndroidApp
MainActivity.kt         ← slimmed down; Hilt @AndroidEntryPoint
```

The `BuildConfig` ad unit ID fields remain here since they're variant-specific.

---

## Dependency Injection Migration (Manual → Hilt)

Current state: manual injection in `SkeinApplication`, factories passed via constructor.
Problem: in a multi-module setup manual wiring across module boundaries becomes unmanageable.

**Recommended: Hilt**

### Steps

1. Add Hilt to root `build.gradle.kts`:
   ```kotlin
   id("com.google.dagger.hilt.android") version "2.51" apply false
   ```
2. Apply `hilt.android` plugin + `hilt.compiler` KSP in each module that needs injection.
3. Annotate `SkeinApplication` with `@HiltAndroidApp`.
4. Annotate `MainActivity` with `@AndroidEntryPoint`.
5. Create Hilt modules:
   - `:data` → `@Module @InstallIn(SingletonComponent)` providing `AppDatabase`, DAOs, `UserPreferencesRepository`
   - `:ads` → `@Module @InstallIn(SingletonComponent)` providing `ConsentManager`, `RewardAdManager`, `InterstitialAdManager`
   - `:feature:game` → `@Module @InstallIn(ViewModelComponent)` providing `GameEngineConfig`
6. Replace manual ViewModel factories with `@HiltViewModel`.
7. Delete `SkeinApplication.database`, `.gameStateDao`, `.rewardAdManager`, etc. — Hilt owns them.

---

## Migration Phases

### Phase 1 — Extract `:core:models` (lowest risk)
- Move pure data classes and constants: `GameModels`, `GenerationParams`, `GameConstants`
- Define repository interfaces here
- No Android deps → runs on pure JVM, all existing unit tests still pass
- Verify: `./gradlew :core:models:test`

### Phase 2 — Extract `:domain`
- Move all pure-Kotlin engine logic (generator, checker, progression, input)
- Port existing engine unit tests to `:domain` test sources
- Verify: `./gradlew :domain:test`

### Phase 3 — Extract `:data`
- Move Room entities, DAOs, repository implementations, migration
- Implement `IUserPreferencesRepository` against the interface defined in `:core:models`
- Verify: `./gradlew :data:test`

### Phase 4 — Extract `:ads`
- Move `ConsentManager`, `RewardAdManager`, `InterstitialAdManager`
- Verify ad loading still works in a debug build

### Phase 5 — Extract `:core:ui`
- Move theme and shared Compose components
- Verify UI renders correctly in a connected debug build

### Phase 6 — Extract features (`:feature:game` first)
- Game is the most complex feature; extract it and ensure `GameEngine` ViewModel works with Hilt
- Then `:feature:settings`, `:feature:generate`, `:feature:home` in any order
- Run full test suite after each feature: `./gradlew lint test detekt`

### Phase 7 — Extract `:navigation`
- Move Appyx `RootNode` and all nodes once all features are stable modules

### Phase 8 — Slim down `:app`
- `:app` should contain only `MainActivity`, `SkeinApplication`, manifest, `BuildConfig` ad IDs
- Remove all direct source files from `:app`

### Phase 9 — Hilt wiring
- Add Hilt DI across all modules (can be done incrementally per feature during phases 6–7)
- Delete manual injection code in `SkeinApplication`

---

## Proposed `settings.gradle.kts` After Migration

```kotlin
include(":app")
include(":navigation")
include(":feature:home")
include(":feature:game")
include(":feature:generate")
include(":feature:settings")
include(":data")
include(":domain")
include(":ads")
include(":core:models")
include(":core:ui")
```

---

## Key Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| `AppViewModel` is referenced from many places | Split its concerns before extracting features; use Hilt `@Singleton` scoped VMs |
| `GameEngine` is both a ViewModel and domain logic | Keep ViewModel shell in `:feature:game`; extract pure logic to `:domain` |
| Appyx nodes receive dependencies from parent via `BuildContext` | Replace with Hilt `@AndroidEntryPoint` on nodes where possible; use `@HiltViewModel` for all ViewModels |
| Room schema export path breaks after restructure | Update `room.schemaLocation` KSP arg in `:data/build.gradle.kts` |
| Ad unit IDs are `BuildConfig` fields in `:app` | Pass them into `:ads` managers at initialization time via Hilt `@Named` bindings or a `AdConfig` data class |
| Test doubles (`FakeGameStateDao`, etc.) currently in `src/test` | Move them to `:domain`'s `testFixtures` source set so `:feature:game` tests can also use them |
| `NavTarget` is `Parcelable` across all features | Keep `NavTarget` in `:navigation`; feature modules must not depend on `:navigation` to avoid cycles |

---

## Definition of Done

- [ ] `./gradlew clean lint test detekt` passes with 0 errors, 0 warnings
- [ ] Module dependency graph has no cycles (verify with `./gradlew :app:dependencies`)
- [ ] `:core:models` and `:domain` have no Android SDK imports
- [ ] Each feature module can be compiled independently without `:app`
- [ ] All existing unit tests pass in their new module locations
- [ ] Manual DI code in `SkeinApplication` is deleted
