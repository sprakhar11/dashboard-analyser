package com.project.analytics.auth.controller

import com.project.analytics.auth.dto.TrackRequest
import com.project.analytics.auth.dto.TrackResponse
import com.project.analytics.auth.security.AuthenticatedUser
import com.project.analytics.auth.service.TrackService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class TrackController(private val trackService: TrackService) {

    @PostMapping("/track")
    fun track(@RequestBody request: TrackRequest): TrackResponse {
        val principal = SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
        return trackService.track(request, principal.userId, principal.authTokenId, principal.browserId)
    }
}
