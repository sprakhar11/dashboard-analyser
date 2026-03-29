package com.project.analytics.auth.service

import com.project.analytics.auth.AlreadyLoggedInException
import com.project.analytics.auth.EmailAlreadyExistsException
import com.project.analytics.auth.InvalidCredentialsException
import com.project.analytics.auth.RegistrationValidationException
import com.project.analytics.auth.dto.*
import com.project.analytics.auth.mapper.AgeBucketMapper
import com.project.analytics.auth.mapper.AuthTokenMapper
import com.project.analytics.auth.mapper.GenderMapper
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
    private val genderMapper: GenderMapper,
    private val ageBucketMapper: AgeBucketMapper,
    private val passwordUtil: PasswordUtil,
    @Value("\${auth.token.expiry-hours}") private val tokenExpiryHours: Long
) {

    fun register(request: RegisterRequest): RegisterResponse {
        if (userMapper.existsActiveByEmail(request.email)) {
            throw EmailAlreadyExistsException()
        }

        val gender = genderMapper.findActiveById(request.genderId!!)
            ?: throw RegistrationValidationException("Invalid gender ID: ${request.genderId}. Use GET /api/analytics/config to see valid options.")

        // Auto-resolve ageBucketId from age for the DB FK constraint
        val ageBucket = ageBucketMapper.findByAge(request.age!!)
            ?: throw RegistrationValidationException("No matching age bucket found for age: ${request.age}")

        val user = User(
            name = request.name,
            email = request.email,
            passwordHash = passwordUtil.hash(request.password),
            age = request.age,
            genderId = request.genderId,
            ageBucketId = ageBucket.id!!
        )
        userMapper.insert(user)

        return RegisterResponse(
            userId = user.id!!,
            name = user.name,
            email = user.email,
            age = user.age!!,
            gender = GenderInfo(id = gender.id!!, code = gender.code, name = gender.name),
            message = "User registered successfully"
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userMapper.findActiveByEmail(request.email)
            ?: throw InvalidCredentialsException()

        if (!passwordUtil.verify(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

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

        val anyActiveToken = authTokenMapper.findAnyActiveByUserId(user.id)
        if (anyActiveToken != null && !user.multipleSessionAllowed) {
            throw AlreadyLoggedInException()
        }

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

        val gender = genderMapper.findActiveById(user.genderId!!)

        return MeResponse(
            userId = user.id!!,
            name = user.name,
            email = user.email,
            age = user.age ?: 0,
            gender = GenderInfo(
                id = gender!!.id!!,
                code = gender.code,
                name = gender.name
            )
        )
    }
}
