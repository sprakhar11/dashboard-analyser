package com.project.analytics.auth.controller

import com.project.analytics.auth.dto.*
import com.project.analytics.auth.service.AnalyticsService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @GetMapping("/config")
    fun getConfig(): AnalyticsConfigResponse {
        return analyticsService.getConfig()
    }

    @GetMapping("/features")
    fun getFeatureTotals(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) toDate: LocalDateTime?,
        @RequestParam(required = false) ageBucketId: Short?,
        @RequestParam(required = false) genderId: Short?
    ): FeatureTotalsResponse {
        return analyticsService.getFeatureTotals(fromDate, toDate, ageBucketId, genderId)
    }

    @GetMapping("/features/{featureId}/trend")
    fun getFeatureTrend(
        @PathVariable featureId: Short,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) toDate: LocalDateTime?,
        @RequestParam bucket: String,
        @RequestParam(required = false) ageBucketId: Short?,
        @RequestParam(required = false) genderId: Short?
    ): FeatureTrendResponse {
        return analyticsService.getFeatureTrend(featureId, fromDate, toDate, bucket, ageBucketId, genderId)
    }

    @GetMapping("/dashboard")
    fun getDashboard(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fromDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) toDate: LocalDateTime?,
        @RequestParam selectedFeatureId: Short,
        @RequestParam(required = false) ageBucketId: Short?,
        @RequestParam(required = false) genderId: Short?
    ): DashboardResponse {
        return analyticsService.getDashboard(fromDate, toDate, selectedFeatureId, ageBucketId, genderId)
    }
}
