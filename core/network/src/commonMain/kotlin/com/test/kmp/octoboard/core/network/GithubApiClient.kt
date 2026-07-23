package com.test.kmp.octoboard.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GithubApiClient(@PublishedApi internal val httpClient: HttpClient) {
    suspend inline fun <reified T> get(
        path: String,
        params: Map<String, String> = emptyMap()
    ): T = httpClient.get(path) {
        params.forEach { (k, v) -> parameter(k, v) }
    }.body()
}
