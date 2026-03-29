package com.project.analytics.auth.controller

import com.project.analytics.auth.AlreadyLoggedInException
import com.project.analytics.auth.AnalyticsValidationException
import com.project.analytics.auth.EmailAlreadyExistsException
import com.project.analytics.auth.EventTypeNotFoundException
import com.project.analytics.auth.FeatureNotFoundException
import com.project.analytics.auth.InvalidCredentialsException
import com.project.analytics.auth.RegistrationValidationException
import com.project.analytics.auth.TrackValidationException
import com.project.analytics.auth.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(error = "EMAIL_ALREADY_EXISTS", message = ex.message ?: "Email is already registered"))
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(error = "INVALID_CREDENTIALS", message = ex.message ?: "Invalid email or password"))
    }

    @ExceptionHandler(AlreadyLoggedInException::class)
    fun handleAlreadyLoggedIn(ex: AlreadyLoggedInException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(error = ex.errorCode, message = ex.message ?: "User is already logged in from another browser"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
            .ifEmpty { "Validation failed" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "VALIDATION_ERROR", message = message))
    }

    @ExceptionHandler(FeatureNotFoundException::class)
    fun handleFeatureNotFound(ex: FeatureNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error = "FEATURE_NOT_FOUND", message = ex.message ?: "Feature not found"))
    }

    @ExceptionHandler(EventTypeNotFoundException::class)
    fun handleEventTypeNotFound(ex: EventTypeNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error = "EVENT_TYPE_NOT_FOUND", message = ex.message ?: "Event type not found"))
    }

    @ExceptionHandler(TrackValidationException::class)
    fun handleTrackValidation(ex: TrackValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "TRACK_VALIDATION_ERROR", message = ex.message ?: "Track validation failed"))
    }

    @ExceptionHandler(AnalyticsValidationException::class)
    fun handleAnalyticsValidation(ex: AnalyticsValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "ANALYTICS_VALIDATION_ERROR", message = ex.message ?: "Analytics validation failed"))
    }

    @ExceptionHandler(RegistrationValidationException::class)
    fun handleRegistrationValidation(ex: RegistrationValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "REGISTRATION_VALIDATION_ERROR", message = ex.message ?: "Registration validation failed"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(error = "INTERNAL_ERROR", message = "An unexpected error occurred"))
    }
}
