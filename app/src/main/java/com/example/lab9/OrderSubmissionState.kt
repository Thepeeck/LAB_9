package com.example.lab9

sealed interface OrderSubmissionState {
    data object Idle : OrderSubmissionState
    data object Loading : OrderSubmissionState
    data object Success : OrderSubmissionState
    data class Error(val message: String) : OrderSubmissionState
}