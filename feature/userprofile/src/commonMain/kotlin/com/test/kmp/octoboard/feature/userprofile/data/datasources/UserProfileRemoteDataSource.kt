package com.test.kmp.octoboard.feature.userprofile.data.datasources

import com.test.kmp.octoboard.core.common.entities.GithubRepository
import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.feature.userprofile.data.models.GithubUserDetailsResponseDto
import com.test.kmp.octoboard.feature.userprofile.data.models.GithubUserRepositoryItemDto
import com.test.kmp.octoboard.feature.userprofile.entities.GithubUserDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class UserProfileRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun fetchUserDetails(
        username: String,
    ): Result<GithubUserDetails> = safeApiCall {
        val response = httpClient.get("users/$username")
        val dto: GithubUserDetailsResponseDto = response.body()
        dto.toGithubUserDetails()
    }

    suspend fun fetchUserRepositories(
        username: String,
        page: Int,
    ): Result<PagedResult<GithubRepository>> = safeApiCall {
        val response = httpClient.get("users/$username/repos") {
            parameter(
                key = "page",
                value = page.toString(),
            )
        }
        val dtos: List<GithubUserRepositoryItemDto> = response.body()

        PagedResult(
            items = dtos.map { it.toGithubRepository() },
            hasNextPage = response.headers.hasNextPage(),
        )
    }
}

private fun GithubUserDetailsResponseDto.toGithubUserDetails() = GithubUserDetails(
    login = login,
    avatarUrl = avatarUrl,
    name = name,
    bio = bio,
    company = company,
    location = location,
    blogUrl = blog,
    publicRepos = publicRepos,
    followers = followers,
    following = following,
    htmlUrl = htmlUrl,
)

private fun GithubUserRepositoryItemDto.toGithubRepository() = GithubRepository(
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
