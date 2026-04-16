package com.fieldsyncpro.presentation.viewmodel

import app.cash.turbine.test
import com.fieldsyncpro.domain.model.AuthUser
import com.fieldsyncpro.domain.repository.AuthRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @MockK lateinit var authRepository: AuthRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userFlow = MutableStateFlow<AuthUser?>(null)

    private val sampleUser = AuthUser(
        uid         = "uid-123",
        email       = "test@example.com",
        displayName = null,
        photoUrl    = null,
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        every { authRepository.currentUser } returns userFlow
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun createViewModel() = AuthViewModel(authRepository)

    // ── currentUser ───────────────────────────────────────────────────────────

    @Test
    fun `currentUser is null initially`() {
        val vm = createViewModel()
        assertNull(vm.currentUser.value)
    }

    @Test
    fun `currentUser reflects auth state changes`() = runTest {
        val vm = createViewModel()
        userFlow.value = sampleUser
        assertEquals(sampleUser, vm.currentUser.value)
    }

    // ── signIn ────────────────────────────────────────────────────────────────

    @Test
    fun `signIn emits NavigateToTaskList on success`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns sampleUser
        val vm = createViewModel()

        vm.effects.test {
            vm.signIn("test@example.com", "password")
            assertEquals(AuthEffect.NavigateToTaskList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signIn sets errorMessage on failure`() = runTest {
        coEvery {
            authRepository.signInWithEmail(any(), any())
        } throws RuntimeException("Invalid credentials")
        val vm = createViewModel()

        vm.signIn("bad@example.com", "wrong")

        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.errorMessage!!.contains("Invalid credentials"))
    }

    @Test
    fun `signIn sets isLoading to false after completion`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns sampleUser
        val vm = createViewModel()

        vm.signIn("test@example.com", "password")

        assertFalse(vm.uiState.value.isLoading)
    }

    // ── createAccount ─────────────────────────────────────────────────────────

    @Test
    fun `createAccount emits NavigateToTaskList on success`() = runTest {
        coEvery { authRepository.createAccountWithEmail(any(), any()) } returns sampleUser
        val vm = createViewModel()

        vm.effects.test {
            vm.createAccount("new@example.com", "pass123")
            assertEquals(AuthEffect.NavigateToTaskList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createAccount sets errorMessage on failure`() = runTest {
        coEvery {
            authRepository.createAccountWithEmail(any(), any())
        } throws RuntimeException("Email already in use")
        val vm = createViewModel()

        vm.createAccount("existing@example.com", "pass123")

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── signOut ───────────────────────────────────────────────────────────────

    @Test
    fun `signOut delegates to authRepository`() = runTest {
        coEvery { authRepository.signOut() } just runs
        val vm = createViewModel()

        vm.signOut()

        coVerify { authRepository.signOut() }
    }

    // ── clearError ────────────────────────────────────────────────────────────

    @Test
    fun `clearError removes errorMessage`() = runTest {
        coEvery {
            authRepository.signInWithEmail(any(), any())
        } throws RuntimeException("Error")
        val vm = createViewModel()
        vm.signIn("x@example.com", "y")

        assertNotNull(vm.uiState.value.errorMessage)
        vm.clearError()
        assertNull(vm.uiState.value.errorMessage)
    }
}
