package com.project.analytics.auth.controller

import com.project.analytics.auth.dto.*
import com.project.analytics.auth.service.AuthService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): RegisterResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        return authService.login(request)
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("auth-token") token: String): Map<String, String> {
        authService.logout(token)
        return mapOf("message" to "Logged out successfully")
    }

    @GetMapping("/me")
    fun me(): MeResponse {
        val userId = SecurityContextHolder.getContext().authentication.principal as Long
        return authService.getCurrentUser(userId)
    }
}
