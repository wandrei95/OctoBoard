package com.test.kmp.octoboard.integration.data

import com.test.kmp.octoboard.core.network.GithubApiClient
import com.test.kmp.octoboard.integration.entities.SmokeRepo

class SmokeRepository(private val api: GithubApiClient) {
    suspend fun fetchKotlinRepo(): SmokeRepo {
        val dto = api.get<SmokeRepoDto>("repos/JetBrains/kotlin")
        return SmokeRepo(
            fullName = dto.fullName,
            description = dto.description,
            stargazersCount = dto.stargazersCount
        )
    }
}
