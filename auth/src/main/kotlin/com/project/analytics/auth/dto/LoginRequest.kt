package com.project.analytics.auth.dto

data class LoginRequest(
    val email: String,
    val password: String,
    val browserId: String
)
