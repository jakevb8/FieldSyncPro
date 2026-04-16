package com.fieldsyncpro.domain.repository

import com.fieldsyncpro.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the current user, or null when signed out. */
    val currentUser: Flow<AuthUser?>

    /** Returns the current Firebase ID token, refreshing if expired. */
    suspend fun getIdToken(): String?

    /**
     * Sign in with a Google ID token obtained from the Google Sign-In SDK.
     * @throws FirebaseAuthException if the credential is invalid.
     */
    suspend fun signInWithGoogle(idToken: String): AuthUser

    /** Sign out the current user. */
    suspend fun signOut()
}
