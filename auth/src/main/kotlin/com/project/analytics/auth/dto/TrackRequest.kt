package com.project.analytics.auth.dto

import java.time.OffsetDateTime

data class TrackRequest(
    val featureId: Short,
    val eventTypeId: Short,
    val eventTime: OffsetDateTime,
    val metaInfo: Map<String, Any>? = null
)
