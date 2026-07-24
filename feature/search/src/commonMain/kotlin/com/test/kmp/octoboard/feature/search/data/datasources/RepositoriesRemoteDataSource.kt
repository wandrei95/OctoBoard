package com.test.kmp.octoboard.feature.search.data.datasources

import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.feature.search.data.models.RepoItemDto
import com.test.kmp.octoboard.feature.search.data.models.RepoSearchResponseDto
import com.test.kmp.octoboard.feature.search.entities.RepoSummary
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class RepositoriesRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun search(
        query: String,
        sort: SearchSort,
        language: String?,
        page: Int,
    ): Result<PagedResult<RepoSummary>> = safeApiCall {
        val params = buildMap {
            put("q", buildRepoSearchQualifiers(query, language))
            put("page", page.toString())
            putAll(sort.toSortParams())
        }
        val response = httpClient.get("search/repositories") {
            params.forEach { (k, v) -> parameter(key = k, value = v) }
        }
        val dto: RepoSearchResponseDto = response.body()

        PagedResult(
            items = dto.items.map { it.toRepoSummary() },
            hasNextPage = response.headers.hasNextPage(),
        )
    }
}

private fun RepoItemDto.toRepoSummary() = RepoSummary(
    id = id,
    name = name,
    fullName = fullName,
    ownerLogin = owner.login,
    ownerAvatarUrl = owner.avatarUrl,
    description = description,
    stars = stargazersCount,
    forks = forksCount,
    language = language,
    htmlUrl = htmlUrl,
)
