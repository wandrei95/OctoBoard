package com.test.kmp.octoboard.feature.search.data.datasources

import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.feature.search.data.models.UserItemDto
import com.test.kmp.octoboard.feature.search.data.models.UserSearchResponseDto
import com.test.kmp.octoboard.feature.search.entities.UserSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class UsersRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun search(query: String, page: Int): Result<PagedResult<UserSummary>> = safeApiCall {
        val params = mapOf("q" to query, "page" to page.toString())
        val response = httpClient.get("search/users") {
            params.forEach { (k, v) -> parameter(key = k, value = v) }
        }
        val dto: UserSearchResponseDto = response.body()

        PagedResult(
            items = dto.items.map { it.toUserSummary() },
            hasNextPage = response.headers.hasNextPage(),
        )
    }
}

private fun UserItemDto.toUserSummary() = UserSummary(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
)
