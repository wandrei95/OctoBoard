package com.test.kmp.octoboard.feature.search.entities

data class UserSummary(
    val id: Long,
    val login: String,
    val avatarUrl: String?,
    val htmlUrl: String,
)
