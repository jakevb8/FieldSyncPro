# FieldSync Pro — OpenCode Rules

## Project Overview
FieldSync Pro is a Modern Android Development (MAD) showcase app for field technicians.
It is a 100% Jetpack Compose, offline-first task management application.

## Architecture Rules
- **Pattern:** MVI (Model-View-Intent) + Clean Architecture (data / domain / presentation layers)
- **UI:** 100% Jetpack Compose with Material 3 — no XML layouts ever
- **Concurrency:** Kotlin Coroutines & Flow (StateFlow for state, SharedFlow for one-shot effects)
- **DI:** Hilt (Dagger) — every dependency must be injected, no manual instantiation in production code
- **Persistence:** Room Database — all queries must be suspend or return Flow
- **Networking:** Retrofit + OkHttp + Kotlinx Serialization
- **Background sync:** WorkManager only

## Coding Standards
- All new Kotlin files use `package com.fieldsynclpro` hierarchy
- Data models live in `domain/model/`
- Room entities live in `data/local/entity/`
- DAOs live in `data/local/dao/`
- Repository interfaces live in `domain/repository/`
- Repository implementations live in `data/repository/`
- API service interfaces live in `data/remote/api/`
- ViewModels live in `presentation/viewmodel/`
- Compose screens live in `presentation/ui/screen/`
- Compose reusable components live in `presentation/ui/component/`
- Hilt modules live in `di/`

## Testing Rules
- **Every feature addition must include unit tests** — no exceptions
- ViewModel tests use Turbine + MockK + `kotlinx-coroutines-test`
- Repository tests mock the DAO and API service with MockK
- DAO tests use Room in-memory database (`Room.inMemoryDatabaseBuilder`)
- Test files mirror the source path under `src/test/` or `src/androidTest/`
- Tests must pass before any commit is made
- Use `@OptIn(ExperimentalCoroutinesApi::class)` and `UnconfinedTestDispatcher` for coroutine tests

## Spec Rules
- When a new feature is added or behaviour changes, **update spec.md** immediately
- Spec sections: Executive Summary, Technical Stack, Core Data Models, Features, Architecture Decisions

## Git / Release Rules
- Run `./gradlew test` (and `./gradlew connectedAndroidTest` when emulator available) before every commit
- Commit messages follow Conventional Commits: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`
- The `main` branch is always in a passing-tests state

## Dependency Versions (keep in sync with libs.versions.toml)
- Kotlin: 1.9.x
- AGP: 8.x
- Compose BOM: 2024.x
- Hilt: 2.51.x
- Room: 2.6.x
- Retrofit: 2.11.x
- WorkManager: 2.9.x
- Turbine: 1.1.x
- MockK: 1.13.x
