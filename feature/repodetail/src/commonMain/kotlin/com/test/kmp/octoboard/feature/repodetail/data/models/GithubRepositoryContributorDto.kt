package com.test.kmp.octoboard.feature.repodetail.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GithubRepositoryContributorDto(
    val id: Long,
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val contributions: Int = 0,
    @SerialName("html_url")
    val htmlUrl: String,
)
