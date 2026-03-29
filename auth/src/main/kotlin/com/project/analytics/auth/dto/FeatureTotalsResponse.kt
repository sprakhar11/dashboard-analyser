package com.project.analytics.auth.dto

import java.time.LocalDateTime

data class FeatureTotalsResponse(
    val success: Boolean,
    val data: FeatureTotalsData
)

data class FeatureTotalsData(
    val summary: FilterSummary,
    val items: List<FeatureTotalItem>
)

data class FilterSummary(
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null,
    val ageBucketId: Short? = null,
    val genderId: Short? = null
)

data class FeatureTotalItem(
    val featureId: Short,
    val featureName: String,
    val totalCount: Long
)
