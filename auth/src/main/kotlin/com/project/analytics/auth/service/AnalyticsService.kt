package com.project.analytics.auth.service

import com.project.analytics.auth.AnalyticsValidationException
import com.project.analytics.auth.FeatureNotFoundException
import com.project.analytics.auth.dto.*
import com.project.analytics.auth.mapper.*
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AnalyticsService(
    private val featureMapper: FeatureMapper,
    private val eventTypeMapper: EventTypeMapper,
    private val genderMapper: GenderMapper,
    private val ageBucketMapper: AgeBucketMapper,
    private val featureEventMapper: FeatureEventMapper
) {

    fun getConfig(): AnalyticsConfigResponse {
        val features = featureMapper.findAllActive().map { FeatureConfigItem(it.id!!, it.name) }
        val eventTypes = eventTypeMapper.findAllActive().map { EventTypeConfigItem(it.id!!, it.name) }
        val ageBuckets = ageBucketMapper.findAllActive().map { AgeBucketConfigItem(it.id!!, it.name) }
        val genders = genderMapper.findAllActive().map { GenderConfigItem(it.id!!, it.name) }

        return AnalyticsConfigResponse(
            success = true,
            data = AnalyticsConfigData(
                features = features,
                eventTypes = eventTypes,
                ageBuckets = ageBuckets,
                genders = genders
            )
        )
    }

    fun getFeatureTotals(
        fromDate: LocalDate,
        toDate: LocalDate,
        ageBucketId: Short?,
        genderId: Short?
    ): FeatureTotalsResponse {
        val items = featureEventMapper.getFeatureTotals(fromDate, toDate, ageBucketId, genderId)
        return FeatureTotalsResponse(
            success = true,
            data = FeatureTotalsData(
                summary = FilterSummary(fromDate, toDate, ageBucketId, genderId),
                items = items
            )
        )
    }

    fun getFeatureTrend(
        featureId: Short,
        fromDate: LocalDate,
        toDate: LocalDate,
        bucket: String,
        ageBucketId: Short?,
        genderId: Short?
    ): FeatureTrendResponse {
        val feature = featureMapper.findActiveById(featureId)
            ?: throw FeatureNotFoundException(featureId)

        if (bucket != "day" && bucket != "hour") {
            throw AnalyticsValidationException("Invalid bucket: $bucket. Must be 'day' or 'hour'")
        }

        val points = when (bucket) {
            "day" -> featureEventMapper.getDailyTrend(featureId, fromDate, toDate, ageBucketId, genderId)
            "hour" -> featureEventMapper.getHourlyTrend(featureId, fromDate, toDate, ageBucketId, genderId)
            else -> emptyList()
        }

        return FeatureTrendResponse(
            success = true,
            data = FeatureTrendData(
                featureId = feature.id!!,
                featureName = feature.name,
                bucket = bucket,
                points = points
            )
        )
    }

    fun getDashboard(
        fromDate: LocalDate,
        toDate: LocalDate,
        selectedFeatureId: Short,
        ageBucketId: Short?,
        genderId: Short?
    ): DashboardResponse {
        val feature = featureMapper.findActiveById(selectedFeatureId)
            ?: throw FeatureNotFoundException(selectedFeatureId)

        val barChart = featureEventMapper.getFeatureTotals(fromDate, toDate, ageBucketId, genderId)
        val trendPoints = featureEventMapper.getDailyTrend(selectedFeatureId, fromDate, toDate, ageBucketId, genderId)

        return DashboardResponse(
            success = true,
            data = DashboardData(
                summary = DashboardSummary(fromDate, toDate, ageBucketId, genderId, selectedFeatureId),
                barChart = barChart,
                lineChart = FeatureTrendData(
                    featureId = feature.id!!,
                    featureName = feature.name,
                    bucket = "day",
                    points = trendPoints
                )
            )
        )
    }
}
