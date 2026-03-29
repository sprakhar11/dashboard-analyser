package com.project.analytics.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be a valid email address")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String = "",

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String = "",

    @field:NotBlank(message = "Browser ID is required")
    @field:Size(max = 100, message = "Browser ID must not exceed 100 characters")
    val browserId: String = ""
)
