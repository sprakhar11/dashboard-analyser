package com.project.analytics.auth.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val passwordHash: String,
    val multipleSessionAllowed: Boolean = false,
    val age: Int? = null,
    val genderId: Short? = null,
    val ageBucketId: Short? = null,
    val addDate: LocalDateTime? = null,
    val deleteInfo: String? = null
)
