package com.test.kmp.octoboard.feature.repodetail.data.datasources

import com.test.kmp.octoboard.core.common.entities.GithubRepository
import com.test.kmp.octoboard.core.githubapi.GithubRepositoryDto
import com.test.kmp.octoboard.core.githubapi.toGithubRepository
import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.feature.repodetail.data.models.GithubRepositoryContributorDto
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryContributor
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryLanguageBreakdown
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class GithubRepositoryDetailRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun fetchRepositoryDetails(
        owner: String,
        repo: String
    ): Result<GithubRepository> = safeApiCall {
        val response = httpClient.get("repos/$owner/$repo")
        val dto: GithubRepositoryDto = response.body()
        dto.toGithubRepository()
    }

    suspend fun fetchRepositoryById(id: Long): Result<GithubRepository> = safeApiCall {
        val response = httpClient.get("repositories/$id")
        val dto: GithubRepositoryDto = response.body()
        dto.toGithubRepository()
    }

    suspend fun fetchLanguages(
        owner: String,
        repo: String
    ): Result<GithubRepositoryLanguageBreakdown> = safeApiCall {
        val response = httpClient.get("repos/$owner/$repo/languages")
        val bytesByLanguage: Map<String, Long> = response.body()
        GithubRepositoryLanguageBreakdown(bytesByLanguage = bytesByLanguage)
    }

    suspend fun fetchContributors(
        owner: String,
        repo: String
    ): Result<List<GithubRepositoryContributor>> = safeApiCall {
        val response = httpClient.get("repos/$owner/$repo/contributors")
        val dtos: List<GithubRepositoryContributorDto> = response.body()
        dtos.map { it.toGithubRepositoryContributor() }
    }
}

private fun GithubRepositoryContributorDto.toGithubRepositoryContributor() =
    GithubRepositoryContributor(
        id = id,
        login = login,
        avatarUrl = avatarUrl,
        contributions = contributions,
        htmlUrl = htmlUrl,
    )
