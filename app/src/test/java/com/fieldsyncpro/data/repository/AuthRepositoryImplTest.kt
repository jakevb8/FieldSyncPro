package com.fieldsyncpro.data.repository

import com.fieldsyncpro.domain.model.AuthUser
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    @MockK lateinit var firebaseAuth: FirebaseAuth
    @MockK lateinit var firebaseUser: FirebaseUser

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: AuthRepositoryImpl

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

        every { firebaseUser.uid }         returns sampleUser.uid
        every { firebaseUser.email }       returns sampleUser.email
        every { firebaseUser.displayName } returns sampleUser.displayName
        every { firebaseUser.photoUrl }    returns null

        repository = AuthRepositoryImpl(firebaseAuth)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    // ── currentUser flow ──────────────────────────────────────────────────────

    @Test
    fun `currentUser emits null when no user signed in`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        }
        every { firebaseAuth.removeAuthStateListener(any()) } just runs
        every { firebaseAuth.currentUser } returns null

        val result = repository.currentUser.first()
        assertNull(result)
    }

    @Test
    fun `currentUser emits AuthUser when user is signed in`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        }
        every { firebaseAuth.removeAuthStateListener(any()) } just runs
        every { firebaseAuth.currentUser } returns firebaseUser

        val result = repository.currentUser.first()
        assertNotNull(result)
        assertEquals(sampleUser.uid, result?.uid)
        assertEquals(sampleUser.email, result?.email)
    }

    // ── getIdToken ────────────────────────────────────────────────────────────

    @Test
    fun `getIdToken returns null when no user`() = runTest {
        every { firebaseAuth.currentUser } returns null
        val token = repository.getIdToken()
        assertNull(token)
    }

    @Test
    fun `getIdToken returns token string when user signed in`() = runTest {
        val mockResult = mockk<GetTokenResult>()
        every { mockResult.token } returns "test-token-abc"
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.getIdToken(false) } returns Tasks.forResult(mockResult)

        val token = repository.getIdToken()
        assertEquals("test-token-abc", token)
    }

    // ── signInWithEmail ───────────────────────────────────────────────────────

    @Test
    fun `signInWithEmail returns AuthUser on success`() = runTest {
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns firebaseUser
        every {
            firebaseAuth.signInWithEmailAndPassword("test@example.com", "password")
        } returns Tasks.forResult(authResult)

        val result = repository.signInWithEmail("test@example.com", "password")
        assertEquals(sampleUser.uid, result.uid)
    }

    @Test(expected = Exception::class)
    fun `signInWithEmail throws on failure`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword(any(), any())
        } returns Tasks.forException(Exception("Invalid credentials"))

        repository.signInWithEmail("bad@example.com", "wrong")
    }

    // ── createAccountWithEmail ────────────────────────────────────────────────

    @Test
    fun `createAccountWithEmail returns AuthUser on success`() = runTest {
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns firebaseUser
        every {
            firebaseAuth.createUserWithEmailAndPassword("new@example.com", "pass123")
        } returns Tasks.forResult(authResult)

        val result = repository.createAccountWithEmail("new@example.com", "pass123")
        assertEquals(sampleUser.uid, result.uid)
    }

    // ── signOut ───────────────────────────────────────────────────────────────

    @Test
    fun `signOut calls firebaseAuth signOut`() = runTest {
        every { firebaseAuth.signOut() } just runs
        repository.signOut()
        verify { firebaseAuth.signOut() }
    }
}
