package com.project.analytics.auth.model

data class Gender(
    val id: Short? = null,
    val code: String,
    val name: String,
    val sortOrder: Short,
    val deleteInfo: String? = null
)
