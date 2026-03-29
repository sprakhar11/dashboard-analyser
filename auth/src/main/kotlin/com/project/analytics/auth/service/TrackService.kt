package com.project.analytics.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.analytics.auth.EventTypeNotFoundException
import com.project.analytics.auth.FeatureNotFoundException
import com.project.analytics.auth.dto.TrackRequest
import com.project.analytics.auth.dto.TrackResponse
import com.project.analytics.auth.dto.TrackResponseData
import com.project.analytics.auth.mapper.EventTypeMapper
import com.project.analytics.auth.mapper.FeatureEventMapper
import com.project.analytics.auth.mapper.FeatureMapper
import com.project.analytics.auth.mapper.UserMapper
import com.project.analytics.auth.model.FeatureEvent
import org.springframework.stereotype.Service

@Service
class TrackService(
    private val featureMapper: FeatureMapper,
    private val eventTypeMapper: EventTypeMapper,
    private val userMapper: UserMapper,
    private val featureEventMapper: FeatureEventMapper
) {
    private val objectMapper = ObjectMapper()

    fun track(request: TrackRequest, userId: Long, authTokenId: Long, browserId: String): TrackResponse {
        featureMapper.findActiveById(request.featureId)
            ?: throw FeatureNotFoundException(request.featureId)

        eventTypeMapper.findActiveById(request.eventTypeId)
            ?: throw EventTypeNotFoundException(request.eventTypeId)

        val user = userMapper.findActiveById(userId)
            ?: throw RuntimeException("User not found: $userId")

        val metaInfoJson = request.metaInfo?.let { objectMapper.writeValueAsString(it) }

        val featureEvent = FeatureEvent(
            userId = userId,
            authTokenId = authTokenId,
            featureId = request.featureId,
            eventTypeId = request.eventTypeId,
            browserId = browserId,
            genderId = user.genderId!!,
            ageBucketId = user.ageBucketId!!,
            eventTime = request.eventTime,
            metaInfo = metaInfoJson
        )

        featureEventMapper.insert(featureEvent)

        return TrackResponse(
            success = true,
            message = "Event tracked successfully",
            data = TrackResponseData(
                eventId = featureEvent.id!!,
                featureId = request.featureId,
                eventTypeId = request.eventTypeId,
                eventTime = request.eventTime
            )
        )
    }
}
