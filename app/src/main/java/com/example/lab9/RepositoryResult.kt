package com.example.lab9

import kotlinx.coroutines.CancellationException

internal suspend fun <T> captureRepositoryResult(
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}