package com.project.analytics.auth.service

import com.project.analytics.auth.AlreadyLoggedInException
import com.project.analytics.auth.EmailAlreadyExistsException
import com.project.analytics.auth.InvalidCredentialsException
import com.project.analytics.auth.dto.*
import com.project.analytics.auth.mapper.AuthTokenMapper
import com.project.analytics.auth.mapper.UserMapper
import com.project.analytics.auth.model.AuthToken
import com.project.analytics.auth.model.User
import com.project.analytics.auth.util.PasswordUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val userMapper: UserMapper,
    private val authTokenMapper: AuthTokenMapper,
    @Value("\${auth.token.expiry-hours}") private val tokenExpiryHours: Long
) {

    fun register(request: RegisterRequest): RegisterResponse {
        if (userMapper.existsActiveByEmail(request.email)) {
            throw EmailAlreadyExistsException()
        }

        val user = User(
            name = request.name,
            email = request.email,
            passwordHash = PasswordUtil.hash(request.password)
        )
        userMapper.insert(user)

        return RegisterResponse(
            userId = user.id!!,
            email = user.email,
            message = "User registered successfully"
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        // 1. Find user by email → not found = InvalidCredentialsException
        val user = userMapper.findActiveByEmail(request.email)
            ?: throw InvalidCredentialsException()

        // 2. Verify password → wrong = InvalidCredentialsException
        if (!PasswordUtil.verify(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        // 3. Check active token for same browser_id → found = return existing (sameBrowserReuse=true)
        val existingBrowserToken = authTokenMapper.findActiveByUserIdAndBrowserId(user.id!!, request.browserId)
        if (existingBrowserToken != null) {
            return LoginResponse(
                userId = user.id,
                email = user.email,
                token = existingBrowserToken.token,
                expiryDate = existingBrowserToken.expiryDate,
                sameBrowserReuse = true
            )
        }

        // 4. Check any active token → found AND !multipleSessionAllowed = AlreadyLoggedInException
        val anyActiveToken = authTokenMapper.findAnyActiveByUserId(user.id)
        if (anyActiveToken != null && !user.multipleSessionAllowed) {
            throw AlreadyLoggedInException()
        }

        // 5. Otherwise create new token (sameBrowserReuse=false)
        val now = LocalDateTime.now()
        val newToken = AuthToken(
            userId = user.id,
            token = UUID.randomUUID().toString(),
            browserId = request.browserId,
            expiryDate = now.plusHours(tokenExpiryHours)
        )
        authTokenMapper.insert(newToken)

        return LoginResponse(
            userId = user.id,
            email = user.email,
            token = newToken.token,
            expiryDate = newToken.expiryDate,
            sameBrowserReuse = false
        )
    }

    fun logout(token: String) {
        val activeToken = authTokenMapper.findActiveByToken(token) ?: return
        authTokenMapper.setLogOutDate(activeToken.id!!, LocalDateTime.now())
    }

    fun getCurrentUser(userId: Long): MeResponse {
        val user = userMapper.findActiveById(userId)
            ?: throw InvalidCredentialsException()

        return MeResponse(
            userId = user.id!!,
            name = user.name,
            email = user.email
        )
    }
}
