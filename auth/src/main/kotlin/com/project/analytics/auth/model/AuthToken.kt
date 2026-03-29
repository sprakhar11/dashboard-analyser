package com.project.analytics.auth.model

import java.time.LocalDateTime

data class AuthToken(
    val id: Long? = null,
    val userId: Long,
    val token: String,
    val browserId: String? = null,
    val addDate: LocalDateTime? = null,
    val logOutDate: LocalDateTime? = null,
    val expiryDate: LocalDateTime,
    val deleteInfo: String? = null
)
