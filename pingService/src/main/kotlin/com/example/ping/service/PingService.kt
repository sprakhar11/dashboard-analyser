package com.example.ping.service

import com.example.ping.mapper.HealthCheckMapper
import com.example.ping.model.PingResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PingService(
    @Value("\${app.name}") private val appName: String,
    @Value("\${git.commit.id.abbrev:unknown}") private val gitCommit: String,
    @Value("\${git.commit.time:unknown}") private val gitCommitTime: String,
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
            appVersion = gitCommit,
            gitCommit = gitCommit,
            gitCommitTime = gitCommitTime,
            timestamp = Instant.now().toString(),
            databaseStatus = dbStatus
        )
    }
}
