package com.project.analytics.auth.dto

data class FeatureTrendResponse(
    val success: Boolean,
    val data: FeatureTrendData
)

data class FeatureTrendData(
    val featureId: Short,
    val featureName: String,
    val bucket: String,
    val points: List<TrendPoint>
)

data class TrendPoint(
    val time: String,
    val count: Long
)
