package com.project.analytics.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    val name: String = "",

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be a valid email address")
    val email: String = "",

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String = "",

    @field:NotNull(message = "Gender ID is required")
    val genderId: Short? = null,

    @field:NotNull(message = "Age is required")
    @field:Min(value = 1, message = "Age must be at least 1")
    @field:Max(value = 150, message = "Age must be at most 150")
    val age: Int? = null
)
