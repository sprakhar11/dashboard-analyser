package com.project.analytics.auth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.analytics.auth.dto.ErrorResponse
import com.project.analytics.auth.mapper.AuthTokenMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthTokenAuthenticationFilter(
    private val authTokenMapper: AuthTokenMapper
) : OncePerRequestFilter() {

    private val objectMapper = ObjectMapper()

    private val publicPaths = setOf("/api/auth/register", "/api/auth/login", "/api/ping")

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI in publicPaths
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("auth-token")

        if (token != null) {
            val authToken = authTokenMapper.findActiveByToken(token)
            if (authToken != null) {
                val authentication = UsernamePasswordAuthenticationToken(
                    authToken.userId, null, emptyList()
                )
                SecurityContextHolder.getContext().authentication = authentication
                filterChain.doFilter(request, response)
                return
            }
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        val errorResponse = ErrorResponse(error = "UNAUTHORIZED", message = "Authentication required")
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
