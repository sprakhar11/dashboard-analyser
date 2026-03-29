package com.project.analytics.auth

class EmailAlreadyExistsException(
    message: String = "Email is already registered"
) : RuntimeException(message)

class InvalidCredentialsException(
    message: String = "Invalid email or password"
) : RuntimeException(message)

class AlreadyLoggedInException(
    val errorCode: String = "USER_ALREADY_LOGGED_IN_ANOTHER_BROWSER",
    message: String = "User is already logged in from another browser"
) : RuntimeException(message)

class FeatureNotFoundException(featureId: Short) :
    RuntimeException("Feature not found: $featureId")

class EventTypeNotFoundException(eventTypeId: Short) :
    RuntimeException("Event type not found: $eventTypeId")

class TrackValidationException(message: String) :
    RuntimeException(message)

class AnalyticsValidationException(message: String) :
    RuntimeException(message)

class RegistrationValidationException(message: String) :
    RuntimeException(message)
