package com.example.lab9

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FirebaseSessionRepository(
    private val auth: FirebaseAuth
) : SessionRepository {

    private val sessionMutex = Mutex()

    override suspend fun ensureAnonymousSession(): Result<String> {
        return sessionMutex.withLock {
            captureRepositoryResult {
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    currentUser.uid
                } else {
                    val result = auth.signInAnonymously().await()

                    val user = checkNotNull(result.user) {
                        "Anonymous authentication returned no user."
                    }

                    user.uid
                }
            }
        }
    }
}