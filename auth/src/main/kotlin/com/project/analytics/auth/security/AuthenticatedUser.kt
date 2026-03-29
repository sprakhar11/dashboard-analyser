package com.project.analytics.auth.security

data class AuthenticatedUser(
    val userId: Long,
    val authTokenId: Long,
    val browserId: String
)
