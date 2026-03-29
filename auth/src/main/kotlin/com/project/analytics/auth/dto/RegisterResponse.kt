package com.project.analytics.auth.dto

data class RegisterResponse(
    val userId: Long,
    val email: String,
    val message: String
)
