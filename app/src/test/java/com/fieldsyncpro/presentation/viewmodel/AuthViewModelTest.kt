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
        displayName = "Test User",
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
        assertNull(createViewModel().currentUser.value)
    }

    @Test
    fun `currentUser reflects auth state changes`() = runTest {
        val vm = createViewModel()
        userFlow.value = sampleUser
        assertEquals(sampleUser, vm.currentUser.value)
    }

    // ── signInWithGoogle ──────────────────────────────────────────────────────

    @Test
    fun `signInWithGoogle emits NavigateToTaskList on success`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } returns sampleUser
        val vm = createViewModel()

        vm.effects.test {
            vm.signInWithGoogle("valid-google-token")
            assertEquals(AuthEffect.NavigateToTaskList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle sets errorMessage on failure`() = runTest {
        coEvery {
            authRepository.signInWithGoogle(any())
        } throws RuntimeException("Invalid Google token")
        val vm = createViewModel()

        vm.signInWithGoogle("bad-token")

        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.errorMessage!!.contains("Invalid Google token"))
    }

    @Test
    fun `signInWithGoogle sets isLoading false after completion`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } returns sampleUser
        val vm = createViewModel()

        vm.signInWithGoogle("token")

        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `signInWithGoogle clears previous error before attempt`() = runTest {
        coEvery { authRepository.signInWithGoogle("bad") } throws RuntimeException("Error")
        coEvery { authRepository.signInWithGoogle("good") } returns sampleUser
        val vm = createViewModel()

        vm.signInWithGoogle("bad")
        assertNotNull(vm.uiState.value.errorMessage)

        vm.signInWithGoogle("good")
        assertNull(vm.uiState.value.errorMessage)
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
        coEvery { authRepository.signInWithGoogle(any()) } throws RuntimeException("Error")
        val vm = createViewModel()
        vm.signInWithGoogle("bad")

        assertNotNull(vm.uiState.value.errorMessage)
        vm.clearError()
        assertNull(vm.uiState.value.errorMessage)
    }
}
