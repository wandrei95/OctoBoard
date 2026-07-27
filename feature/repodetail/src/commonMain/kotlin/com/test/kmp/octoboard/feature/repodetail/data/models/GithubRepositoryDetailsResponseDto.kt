package com.test.kmp.octoboard.feature.repodetail.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubRepositoryDetailsResponseDto(
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
    @SerialName("open_issues_count")
    val openIssuesCount: Int = 0,
    @SerialName("default_branch")
    val defaultBranch: String,
    @SerialName("html_url")
    val htmlUrl: String,
)

@Serializable
internal data class GithubRepositoryOwnerDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
