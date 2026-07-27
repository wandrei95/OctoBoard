package com.test.kmp.octoboard.feature.userprofile.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubUserRepositoryItemDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int = 0,
    @SerialName("forks_count")
    val forksCount: Int = 0,
    val language: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
)
