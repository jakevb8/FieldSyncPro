# Project Spec: FieldSync Pro (Modern Android)

## 1. Executive Summary
**FieldSync Pro** is a high-performance task management application built for field technicians. It serves as a technical showcase for **Modern Android Development (MAD)**, focusing on a 100% declarative UI, robust offline-first synchronization, and type-safe state management.

## 2. Technical Stack
* **UI:** 100% Jetpack Compose (Material 3)
* **Architecture:** MVI (Model-View-Intent) with Clean Architecture (data / domain / presentation layers)
* **Concurrency:** Kotlin Coroutines & Flow (StateFlow for state, SharedFlow/Channel for one-shot effects)
* **Dependency Injection:** Hilt (Dagger)
* **Local Persistence:** Room Database (SQLite-based) with TypeConverters for `TaskVibe` and `TaskStatus`
* **Networking:** Retrofit + OkHttp + Kotlinx Serialization
* **Background Tasks:** WorkManager — periodic 15-minute sync with exponential back-off
* **Testing:** Turbine (Flow testing) + MockK + `kotlinx-coroutines-test`

## 3. Core Data Models (Kotlin)

```kotlin
sealed interface TaskVibe {
    object Hype : TaskVibe    // Urgent/Immediate
    object Steady : TaskVibe  // Standard
    object Chill : TaskVibe   // Low priority
}

data class FieldTask(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val vibe: TaskVibe,
    val lastSynced: Long,
    val isLocalOnly: Boolean // Optimistic UI flag
)

enum class TaskStatus { PENDING, SYNCING, COMPLETED, CONFLICT }
```

## 4. Features Implemented

### 4.1 Task List Screen
* Reactive list of all `FieldTask` objects backed by Room (single source of truth)
* Pull-to-sync button (manual sync trigger) with loading indicator
* Per-task quick-actions: mark complete, delete
* Filter bar with `TaskVibeFilter` chips: ALL / HYPE / STEADY / CHILL
* Empty-state message when no tasks exist

### 4.2 Task Detail / Create Screen
* Create new tasks (assigned a UUID locally, `isLocalOnly = true`)
* Edit existing tasks: title, description, vibe, status
* Save button disabled when title is blank or a save is in-flight
* Delete button for existing tasks

### 4.3 Offline-First Sync
* All writes go to Room first (optimistic)
* `TaskSyncWorker` (WorkManager) pushes `isLocalOnly` tasks then pulls remote state every 15 minutes
* Tasks marked `SYNCING` during upload; reverted to `PENDING` on network failure
* Last-write-wins merge strategy on remote pull

### 4.4 Navigation
* Jetpack Navigation Compose with two top-level destinations: Task List and Task Detail/Create

### 4.5 Authentication (Firebase + Google Sign-In)
* Google Sign-In only — users tap "Continue with Google", the Google Sign-In SDK presents the account picker, and the resulting ID token is exchanged with Firebase Auth via `signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))`
* `AuthRepository` interface with `currentUser: Flow<AuthUser?>`, `signInWithGoogle(idToken)`, `getIdToken`, `signOut`
* `AuthRepositoryImpl` uses `FirebaseAuth` + `callbackFlow` to emit live auth state, and `GoogleAuthProvider` + `signInWithCredential` for sign-in
* `AuthViewModel` exposes `currentUser: StateFlow<AuthUser?>`, `uiState` (loading/error), and one-shot `AuthEffect.NavigateToTaskList`
* `LoginScreen` — Compose screen with a single "Continue with Google" button using `rememberLauncherForActivityResult` to launch the `GoogleSignInClient` intent
* Nav graph uses `currentUser` state to gate all task routes: unauthenticated users are redirected to `login`, authenticated users are redirected away from it
* `FieldSyncProApplication` observes auth state to schedule `TaskSyncWorker` only when signed in, and cancels it on sign-out
* `NetworkModule` OkHttp interceptor attaches `Authorization: Bearer <token>` on every API request using `runBlocking { authRepository.getIdToken() }`

### 4.6 Hilt Modules (updated)
* `AuthModule` — provides `FirebaseAuth` instance, binds `AuthRepositoryImpl` to `AuthRepository`

## 5. Architecture Decisions

### MVI Contract
Each screen has a sealed `UiState`, sealed `Intent`, and sealed `Effect`:
* `TaskListUiState` / `TaskListIntent` / `TaskListEffect`
* `TaskDetailUiState` / `TaskDetailIntent` / `TaskDetailEffect`

### Clean Architecture Layers
```
presentation/  ← ViewModels, Compose screens, MVI contracts
domain/        ← FieldTask, TaskVibe, TaskStatus, use cases, repository interface
data/          ← Room entities, DAOs, Retrofit DTOs, repository implementation, mapper
worker/        ← TaskSyncWorker
di/            ← Hilt modules
```

### Dependency Injection
Four Hilt modules in `di/`:
* `DatabaseModule` — Room database + DAO bindings
* `NetworkModule` — OkHttp (with auth interceptor) + Retrofit + Kotlinx JSON
* `RepositoryModule` — binds `TaskRepositoryImpl` to `TaskRepository`
* `AuthModule` — provides `FirebaseAuth`, binds `AuthRepositoryImpl` to `AuthRepository`
* `WorkManagerModule` — provides `WorkManager`

## 6. Testing Coverage

| Layer | Test class | Strategy |
|---|---|---|
| Data mapper | `TaskMapperTest` | Pure unit tests |
| Room converters | `TaskTypeConvertersTest` | Pure unit tests |
| Repository | `TaskRepositoryImplTest` | MockK (DAO + API) |
| Use cases | `UseCaseTest` | MockK repository |
| TaskListViewModel | `TaskListViewModelTest` | Turbine + MockK + UnconfinedTestDispatcher |
| TaskDetailViewModel | `TaskDetailViewModelTest` | Turbine + MockK + UnconfinedTestDispatcher |
| AuthRepositoryImpl | `AuthRepositoryImplTest` | MockK (FirebaseAuth + FirebaseUser + Tasks) |
| AuthViewModel | `AuthViewModelTest` | Turbine + MockK + UnconfinedTestDispatcher |
