package com.test.kmp.octoboard.feature.search.data.datasources

import com.test.kmp.octoboard.core.common.entities.GithubRepository
import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.feature.search.data.models.GithubRepositoryItemDto
import com.test.kmp.octoboard.feature.search.data.models.GithubRepositorySearchResponseDto
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class GithubRepositorySearchRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun search(
        query: String,
        sort: SearchSort,
        language: String?,
        page: Int,
    ): Result<PagedResult<GithubRepository>> = safeApiCall {
        val params = buildMap {
            put(
                key = "q",
                value = buildRepoSearchQualifiers(
                    query = query,
                    language = language,
                ),
            )
            put(
                key = "page",
                value = page.toString(),
            )
            putAll(sort.toSortParams())
        }
        val response = httpClient.get("search/repositories") {
            params.forEach { (k, v) ->
                parameter(
                    key = k,
                    value = v,
                )
            }
        }
        val dto: GithubRepositorySearchResponseDto = response.body()

        PagedResult(
            items = dto.items.map { it.toGithubRepository() },
            hasNextPage = response.headers.hasNextPage(),
        )
    }
}

private fun GithubRepositoryItemDto.toGithubRepository() = GithubRepository(
    id = id,
    name = name,
    fullName = fullName,
    ownerLogin = owner.login,
    ownerAvatarUrl = owner.avatarUrl,
    description = description,
    stars = stargazersCount,
    forks = forksCount,
    language = language,
    openIssues = openIssuesCount,
    defaultBranch = defaultBranch,
    htmlUrl = htmlUrl,
)
