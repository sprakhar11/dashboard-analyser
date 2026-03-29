package com.project.analytics.auth.dto

import java.time.OffsetDateTime

data class TrackResponse(
    val success: Boolean,
    val message: String,
    val data: TrackResponseData? = null
)

data class TrackResponseData(
    val eventId: Long,
    val featureId: Short,
    val eventTypeId: Short,
    val eventTime: OffsetDateTime
)
