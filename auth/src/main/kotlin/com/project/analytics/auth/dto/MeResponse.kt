package com.project.analytics.auth.dto

data class MeResponse(
    val userId: Long,
    val name: String,
    val email: String,
    val age: Int,
    val gender: GenderInfo
)
