package com.test.kmp.octoboard.feature.search.data.datasources

import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.feature.search.data.models.GithubUserItemDto
import com.test.kmp.octoboard.feature.search.data.models.GithubUserSearchResponseDto
import com.test.kmp.octoboard.feature.search.entities.GithubUserSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class GithubUserSearchRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun search(
        query: String,
        page: Int,
    ): Result<PagedResult<GithubUserSummary>> = safeApiCall {
        val params = mapOf(
            "q" to query,
            "page" to page.toString(),
        )
        val response = httpClient.get("search/users") {
            params.forEach { (k, v) ->
                parameter(
                    key = k,
                    value = v,
                )
            }
        }
        val dto: GithubUserSearchResponseDto = response.body()

        PagedResult(
            items = dto.items.map { it.toGithubUserSummary() },
            hasNextPage = response.headers.hasNextPage(),
        )
    }
}

private fun GithubUserItemDto.toGithubUserSummary() = GithubUserSummary(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
)
