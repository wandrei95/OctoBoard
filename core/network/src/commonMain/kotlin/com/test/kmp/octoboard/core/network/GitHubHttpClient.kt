package com.test.kmp.octoboard.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createGitHubHttpClient(
    tokenProvider: AuthTokenProvider
): HttpClient = HttpClient(provideHttpClientEngine()) {
    expectSuccess = true

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    defaultRequest { url("https://api.github.com/") }

    install(createClientPlugin("AuthInterceptor") {
        onRequest { request, _ ->
            tokenProvider.getToken()?.let { token ->
                request.headers.append(
                    HttpHeaders.Authorization,
                    "token $token",
                )
            }
        }
    })

    install(Logging) { level = LogLevel.ALL }
}
