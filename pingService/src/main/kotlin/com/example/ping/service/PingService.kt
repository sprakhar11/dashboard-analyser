package com.example.ping.service

import com.example.ping.mapper.HealthCheckMapper
import com.example.ping.model.PingResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Properties

@Service
class PingService(
    @Value("\${app.name}") private val appName: String,
    private val healthCheckMapper: HealthCheckMapper
) {
    private val gitProps: Properties = Properties().apply {
        try {
            ClassPathResource("git.properties").inputStream.use { load(it) }
        } catch (_: Exception) { }
    }

    private val gitCommit = gitProps.getProperty("git.commit.id.abbrev") ?: "unknown"

    private val gitCommitTime = gitProps.getProperty("git.commit.time") ?: "unknown"

    fun getPingResponse(): PingResponse {
        val dbStatus = try {
            healthCheckMapper.ping()
            "connected"
        } catch (e: Exception) {
            "disconnected"
        }
        return PingResponse(
            appName = appName,
            appVersion = gitCommit,
            gitCommit = gitCommit,
            gitCommitTime = gitCommitTime,
            timestamp = Instant.now().toString(),
            databaseStatus = dbStatus
        )
    }
}
