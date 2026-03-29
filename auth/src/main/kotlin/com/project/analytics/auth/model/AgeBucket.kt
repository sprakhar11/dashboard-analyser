package com.project.analytics.auth.model

data class AgeBucket(
    val id: Short? = null,
    val code: String,
    val name: String,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val sortOrder: Short,
    val deleteInfo: String? = null
)
