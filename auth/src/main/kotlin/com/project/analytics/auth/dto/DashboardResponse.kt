package com.project.analytics.auth.dto

import java.time.LocalDate

data class DashboardResponse(
    val success: Boolean,
    val data: DashboardData
)

data class DashboardData(
    val summary: DashboardSummary,
    val barChart: List<FeatureTotalItem>,
    val lineChart: FeatureTrendData
)

data class DashboardSummary(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val ageBucketId: Short? = null,
    val genderId: Short? = null,
    val selectedFeatureId: Short
)
