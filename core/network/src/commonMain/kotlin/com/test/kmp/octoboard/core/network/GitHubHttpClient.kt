package com.test.kmp.octoboard.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createGitHubHttpClient(
    tokenProvider: AuthTokenProvider,
    onRateLimitUpdate: (RateLimitInfo) -> Unit
): HttpClient = HttpClient(provideHttpClientEngine()) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    defaultRequest { url("https://api.github.com/") }

    install(createClientPlugin("AuthInterceptor") {
        onRequest { request, _ ->
            tokenProvider.getToken()?.let { token ->
                request.headers.append(HttpHeaders.Authorization, "token $token")
            }
        }
    })

    install(createClientPlugin("RateLimitInterceptor") {
        onResponse { response ->
            val remaining = response.headers["X-RateLimit-Remaining"]?.toIntOrNull()
            val reset = response.headers["X-RateLimit-Reset"]?.toLongOrNull()
            if (remaining != null && reset != null) {
                onRateLimitUpdate(RateLimitInfo(remaining, reset))
            }
        }
    })

    install(Logging) { level = LogLevel.INFO }
}
