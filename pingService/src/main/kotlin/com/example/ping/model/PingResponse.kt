package com.example.ping.model

data class PingResponse(
    val appName: String,
    val appVersion: String,
    val gitCommit: String,
    val gitCommitTime: String,
    val timestamp: String,
    val databaseStatus: String
)
