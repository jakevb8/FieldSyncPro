package com.fieldsyncpro.data.repository

import com.fieldsyncpro.domain.model.AuthUser
import com.fieldsyncpro.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun getIdToken(): String? =
        firebaseAuth.currentUser?.getIdToken(false)?.await()?.token

    override suspend fun signInWithEmail(email: String, password: String): AuthUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user?.toAuthUser()
            ?: error("Sign-in succeeded but user is null")
    }

    override suspend fun createAccountWithEmail(email: String, password: String): AuthUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.toAuthUser()
            ?: error("Account creation succeeded but user is null")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
}
