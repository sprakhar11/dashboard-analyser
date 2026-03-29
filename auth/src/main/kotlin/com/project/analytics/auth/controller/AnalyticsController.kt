package com.project.analytics.auth.controller

import com.project.analytics.auth.dto.*
import com.project.analytics.auth.service.AnalyticsService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @GetMapping("/config")
    fun getConfig(): AnalyticsConfigResponse {
        return analyticsService.getConfig()
    }

    @GetMapping("/features")
    fun getFeatureTotals(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate,
        @RequestParam(required = false) ageBucketId: Short?,
        @RequestParam(required = false) genderId: Short?
    ): FeatureTotalsResponse {
        return analyticsService.getFeatureTotals(fromDate, toDate, ageBucketId, genderId)
    }

    @GetMapping("/features/{featureId}/trend")
    fun getFeatureTrend(
        @PathVariable featureId: Short,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate,
        @RequestParam bucket: String,
        @RequestParam(required = false) ageBucketId: Short?,
        @RequestParam(required = false) genderId: Short?
    ): FeatureTrendResponse {
        return analyticsService.getFeatureTrend(featureId, fromDate, toDate, bucket, ageBucketId, genderId)
    }

    @GetMapping("/dashboard")
    fun getDashboard(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate,
        @RequestParam selectedFeatureId: Short,
        @RequestParam(required = false) ageBucketId: Short?,
        @RequestParam(required = false) genderId: Short?
    ): DashboardResponse {
        return analyticsService.getDashboard(fromDate, toDate, selectedFeatureId, ageBucketId, genderId)
    }
}
