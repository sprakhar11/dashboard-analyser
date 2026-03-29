package com.example.ping.service

import com.example.ping.mapper.HealthCheckMapper
import com.example.ping.model.PingResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PingService(
    @Value("\${app.name}") private val appName: String,
    @Value("\${app.version}") private val appVersion: String,
    @Value("\${app.git-commit:unknown}") private val gitCommit: String,
    private val healthCheckMapper: HealthCheckMapper
) {
    fun getPingResponse(): PingResponse {
        val dbStatus = try {
            healthCheckMapper.ping()
            "connected"
        } catch (e: Exception) {
            "disconnected"
        }
        return PingResponse(
            appName = appName,
            appVersion = appVersion,
            gitCommit = gitCommit,
            timestamp = Instant.now().toString(),
            databaseStatus = dbStatus
        )
    }
}
