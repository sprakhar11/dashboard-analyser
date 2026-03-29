package com.project.analytics.auth.dto

data class RegisterResponse(
    val userId: Long,
    val name: String,
    val email: String,
    val age: Int,
    val gender: GenderInfo,
    val message: String
)

data class GenderInfo(
    val id: Short,
    val code: String,
    val name: String
)

data class AgeBucketInfo(
    val id: Short,
    val code: String,
    val name: String
)
