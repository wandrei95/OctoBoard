package com.test.kmp.octoboard.feature.search.data.models

import com.test.kmp.octoboard.shared.githubapi.GithubRepositoryDto
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubRepositorySearchResponseDto(
    val items: List<GithubRepositoryDto> = emptyList(),
)
