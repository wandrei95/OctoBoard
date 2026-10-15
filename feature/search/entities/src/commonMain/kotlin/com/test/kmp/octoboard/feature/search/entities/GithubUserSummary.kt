package com.test.kmp.octoboard.feature.search.entities

data class GithubUserSummary(
    val id: Long,
    val login: String,
    val avatarUrl: String?,
    val htmlUrl: String,
)
