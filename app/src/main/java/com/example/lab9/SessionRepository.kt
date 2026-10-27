package com.example.lab9

interface SessionRepository {
    suspend fun ensureAnonymousSession(): Result<String>
}