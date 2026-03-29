package com.project.analytics.auth.model

import java.time.LocalDate
import java.time.OffsetDateTime

data class FeatureEvent(
    val id: Long? = null,
    val userId: Long,
    val authTokenId: Long,
    val featureId: Short,
    val eventTypeId: Short,
    val browserId: String,
    val genderId: Short,
    val ageBucketId: Short,
    val eventTime: OffsetDateTime,
    val eventDate: LocalDate? = null,
    val eventHour: Short? = null,
    val metaInfo: String? = null,
    val deleteInfo: String? = null
)
