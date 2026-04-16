# FieldSync Pro — Android

> A high-performance task management app for field technicians. Built as a **Modern Android Development (MAD)** showcase featuring 100% Jetpack Compose, offline-first sync, and type-safe MVI state management.

---

## Screenshots

> _Add screenshots here once the app is running on a device or emulator._

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose (Material 3) — 100% declarative, no XML |
| Architecture | MVI + Clean Architecture (data / domain / presentation) |
| Concurrency | Kotlin Coroutines & Flow (StateFlow / Channel) |
| DI | Hilt (Dagger) |
| Local DB | Room (SQLite) with custom TypeConverters |
| Networking | Retrofit + OkHttp + Kotlinx Serialization |
| Background Sync | WorkManager — periodic 15-min sync, exponential back-off |
| Testing | Turbine · MockK · kotlinx-coroutines-test |

---

## Architecture

FieldSync Pro uses **Clean Architecture** split into three layers with a strict inward dependency rule:

```
┌──────────────────────────────────────────────────┐
│  presentation/                                   │
│   ├── viewmodel/   (MVI: State · Intent · Effect)│
│   └── ui/          (Compose screens · components)│
├──────────────────────────────────────────────────┤
│  domain/                                         │
│   ├── model/       (FieldTask · TaskVibe · Status)│
│   ├── usecase/     (GetAll · Create · Update …)  │
│   └── repository/  (interface only)              │
├──────────────────────────────────────────────────┤
│  data/                                           │
│   ├── local/       (Room DB · DAO · entities)    │
│   ├── remote/      (Retrofit API · DTOs)         │
│   └── repository/  (TaskRepositoryImpl · Mapper) │
├──────────────────────────────────────────────────┤
│  worker/           (TaskSyncWorker — WorkManager)│
│  di/               (4 Hilt modules)              │
└──────────────────────────────────────────────────┘
```

### MVI Pattern

Each screen owns a contract file that defines three sealed types:

```kotlin
// State  — the single source of truth for the UI
data class TaskListUiState(
    val tasks: List<FieldTask> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val activeFilter: TaskVibeFilter = TaskVibeFilter.ALL
)

// Intent — every user action / event
sealed interface TaskListIntent {
    object TriggerSync : TaskListIntent
    data class DeleteTask(val id: String) : TaskListIntent
    data class FilterByVibe(val filter: TaskVibeFilter) : TaskListIntent
    // …
}

// Effect — one-shot events that should not be replayed (snackbars, navigation)
sealed interface TaskListEffect {
    data class ShowSnackbar(val message: String) : TaskListEffect
    data class NavigateToDetail(val taskId: String) : TaskListEffect
}
```

### Offline-First Sync

```
User creates task
      │
      ▼
Room DB (isLocalOnly = true)  ←── Single source of truth for UI
      │
      ▼
TaskSyncWorker (WorkManager)
      │  1. Upload local-only tasks → API (marks SYNCING)
      │  2. Pull remote tasks       → merge into Room
      ▼
Room DB updated  →  UI automatically reflects changes via Flow
```

---

## Core Data Models

```kotlin
sealed interface TaskVibe {
    object Hype   : TaskVibe   // Urgent / immediate
    object Steady : TaskVibe   // Standard
    object Chill  : TaskVibe   // Low priority
}

enum class TaskStatus { PENDING, SYNCING, COMPLETED, CONFLICT }

data class FieldTask(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val vibe: TaskVibe,
    val lastSynced: Long,        // epoch-millis, 0 = never synced
    val isLocalOnly: Boolean     // optimistic UI flag
)
```

---

## Project Structure

```
app/src/main/java/com/fieldsyncpro/
├── di/                          # Hilt modules
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
│   ├── AuthModule.kt
│   └── WorkManagerModule.kt
├── domain/
│   ├── model/                   # FieldTask · TaskVibe · TaskStatus · AuthUser
│   ├── repository/              # TaskRepository · AuthRepository (interfaces)
│   └── usecase/                 # GetAll · Create · Update · Delete · Sync
├── data/
│   ├── local/                   # Room: FieldSyncDatabase · TaskDao · TaskEntity
│   ├── remote/                  # Retrofit: TaskApiService · TaskDto
│   └── repository/              # TaskRepositoryImpl · AuthRepositoryImpl · TaskMapper
├── presentation/
│   ├── viewmodel/               # TaskListViewModel · TaskDetailViewModel · AuthViewModel · contracts
│   └── ui/
│       ├── screen/              # TaskListScreen · TaskDetailScreen · LoginScreen
│       ├── component/           # TaskCard · VibeChip · TaskStatusChip
│       └── theme/               # FieldSyncProTheme (Material 3)
└── worker/
    └── TaskSyncWorker.kt        # WorkManager coroutine worker
```

---

## Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17+ |
| Android SDK | API 26+ (minSdk) / API 34 (compileSdk) |

### Clone & Open

```bash
git clone https://github.com/jakevb8/FieldSyncPro.git
cd FieldSyncPro
```

Open the project in Android Studio and let Gradle sync.

### Configure Firebase

The app uses Firebase Authentication. The `google-services.json` file is already placed at `app/google-services.json` (linked to Firebase project `fieldsyncpro-platform`).

> **Important:** Before signing in you must enable the **Google** sign-in provider in the [Firebase Console](https://console.firebase.google.com/project/fieldsyncpro-platform/authentication/providers), and add your debug SHA-1 fingerprint to the Android app in [Project Settings](https://console.firebase.google.com/project/fieldsyncpro-platform/settings/general). Then re-download `google-services.json`. Run `./gradlew signingReport` to get your SHA-1.

### Configure the API Base URL

The base URL is set in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.fieldsyncpro.example.com/v1/\"")
```

For local development, point this at your running backend (see [FieldSyncPro Platform](https://github.com/jakevb8/FieldSyncProPlatform)):

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3001/v1/\"")
// 10.0.2.2 is the Android emulator alias for localhost
```

### Build & Run

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build + install on connected device/emulator
```

---

## Running Tests

```bash
./gradlew test                   # All unit tests (JVM)
./gradlew connectedAndroidTest   # Instrumented tests (requires emulator/device)
```

### Test Coverage

| Test Class | Tests | Strategy |
|---|---|---|
| `TaskTypeConvertersTest` | 8 | Pure unit — Room TypeConverter round-trips |
| `TaskMapperTest` | 8 | Pure unit — DTO ↔ Entity ↔ Domain mapping |
| `TaskRepositoryImplTest` | 12 | MockK — mocks DAO and API service |
| `UseCaseTest` | 6 | MockK — delegates to repository |
| `TaskListViewModelTest` | 12 | Turbine + MockK + UnconfinedTestDispatcher |
| `TaskDetailViewModelTest` | 13 | Turbine + MockK + UnconfinedTestDispatcher |
| `AuthRepositoryImplTest` | 7 | MockK — mocks FirebaseAuth, GoogleAuthProvider, Tasks |
| `AuthViewModelTest` | 7 | Turbine + MockK + UnconfinedTestDispatcher |
| **Total** | **75** | **0 failures** |

---

## Related Repositories

| Repo | Description |
|---|---|
| [FieldSyncProPlatform](https://github.com/jakevb8/FieldSyncProPlatform) | Monorepo: Express REST API + Next.js web dashboard |

---

## Dependency Versions

| Library | Version |
|---|---|
| Kotlin | 1.9.23 |
| AGP | 8.3.2 |
| Compose BOM | 2024.05.00 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| Retrofit | 2.11.0 |
| WorkManager | 2.9.0 |
| Firebase BOM | 33.1.0 |
| play-services-auth | 21.2.0 |
| Turbine | 1.1.0 |
| MockK | 1.13.11 |

---

## License

```
Copyright 2026 FieldSync Pro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
