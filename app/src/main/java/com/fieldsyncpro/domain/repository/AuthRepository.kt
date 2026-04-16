package com.fieldsyncpro.domain.repository

import com.fieldsyncpro.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the current user, or null when signed out. */
    val currentUser: Flow<AuthUser?>

    /** Returns the current Firebase ID token, refreshing if expired. */
    suspend fun getIdToken(): String?

    /**
     * Sign in with email and password.
     * @throws FirebaseAuthException on bad credentials.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthUser

    /**
     * Create a new account with email and password.
     */
    suspend fun createAccountWithEmail(email: String, password: String): AuthUser

    /** Sign out the current user. */
    suspend fun signOut()
}
