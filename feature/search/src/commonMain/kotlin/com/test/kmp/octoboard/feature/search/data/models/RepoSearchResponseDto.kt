package com.test.kmp.octoboard.feature.search.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RepoSearchResponseDto(
    val items: List<RepoItemDto> = emptyList(),
)

@Serializable
internal data class RepoItemDto(
    val id: Long,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val owner: OwnerDto,
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
internal data class OwnerDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
