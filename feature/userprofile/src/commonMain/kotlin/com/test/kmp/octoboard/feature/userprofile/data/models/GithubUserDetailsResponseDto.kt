package com.test.kmp.octoboard.feature.userprofile.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubUserDetailsResponseDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    val blog: String? = null,
    @SerialName("public_repos")
    val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    @SerialName("html_url")
    val htmlUrl: String,
)
