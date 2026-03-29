package com.project.analytics.auth.dto

import java.time.LocalDateTime

data class LoginResponse(
    val userId: Long,
    val email: String,
    val token: String,
    val expiryDate: LocalDateTime,
    val sameBrowserReuse: Boolean
)
