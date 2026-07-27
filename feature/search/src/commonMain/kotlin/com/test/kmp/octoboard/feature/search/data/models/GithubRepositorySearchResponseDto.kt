package com.test.kmp.octoboard.feature.search.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubRepositorySearchResponseDto(
    val items: List<GithubRepositoryItemDto> = emptyList(),
)

@Serializable
internal data class GithubRepositoryItemDto(
    val id: Long,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val owner: GithubRepositoryOwnerDto,
    val description: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int = 0,
    @SerialName("forks_count")
    val forksCount: Int = 0,
    val language: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
)

@Serializable
internal data class GithubRepositoryOwnerDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
