package com.test.kmp.octoboard.feature.search.data.models

import com.test.kmp.octoboard.core.githubapi.GithubRepositoryDto
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubRepositorySearchResponseDto(
    val items: List<GithubRepositoryDto> = emptyList(),
)
