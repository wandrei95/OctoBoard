package com.test.kmp.octoboard.feature.favorites.data.datasources

import com.test.kmp.octoboard.core.network.safeApiCall
import com.test.kmp.octoboard.shared.entities.GithubRepository
import com.test.kmp.octoboard.shared.githubapi.GithubRepositoryDto
import com.test.kmp.octoboard.shared.githubapi.toGithubRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class FavoritesRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun fetchRepositoryById(id: Long): Result<GithubRepository> = safeApiCall {
        val response = httpClient.get("repositories/$id")
        val dto: GithubRepositoryDto = response.body()
        dto.toGithubRepository()
    }
}
