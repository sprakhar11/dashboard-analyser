package com.example.ping.controller

import com.example.ping.model.PingResponse
import com.example.ping.service.PingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class PingController(private val pingService: PingService) {
    @GetMapping("/ping")
    fun ping(): PingResponse = pingService.getPingResponse()
}
