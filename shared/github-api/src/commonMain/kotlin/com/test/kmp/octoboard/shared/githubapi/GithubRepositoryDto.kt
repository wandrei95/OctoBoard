package com.test.kmp.octoboard.shared.githubapi

import com.test.kmp.octoboard.shared.entities.GithubRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRepositoryDto(
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
    @SerialName("open_issues_count")
    val openIssuesCount: Int = 0,
    @SerialName("default_branch")
    val defaultBranch: String,
    @SerialName("html_url")
    val htmlUrl: String,
)

@Serializable
data class GithubRepositoryOwnerDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)

fun GithubRepositoryDto.toGithubRepository() = GithubRepository(
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
