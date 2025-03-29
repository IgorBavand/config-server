package com.igorbavand.springcloudconfigserver.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestFilter(
    @Value("\${api-token}")
    private val apiToken: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if ((request.getHeader("x-config-token") ?: "") == apiToken) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.reasonPhrase)
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return "/encrypt" == request.requestURI
    }
}