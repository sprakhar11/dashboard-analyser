package com.project.analytics.auth.dto

data class AnalyticsConfigResponse(
    val success: Boolean,
    val data: AnalyticsConfigData
)

data class AnalyticsConfigData(
    val features: List<FeatureConfigItem>,
    val eventTypes: List<EventTypeConfigItem>,
    val ageBuckets: List<AgeBucketConfigItem>,
    val genders: List<GenderConfigItem>
)

data class FeatureConfigItem(val id: Short, val name: String)
data class EventTypeConfigItem(val id: Short, val name: String)
data class AgeBucketConfigItem(val id: Short, val name: String)
data class GenderConfigItem(val id: Short, val name: String)
