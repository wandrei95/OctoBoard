package com.test.kmp.octoboard.feature.repodetail.entities

internal data class GithubRepositoryContributor(
    val id: Long,
    val login: String,
    val avatarUrl: String?,
    val contributions: Int,
    val htmlUrl: String,
)
