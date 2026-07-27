package com.test.kmp.octoboard.feature.userprofile.entities

data class GithubUserRepositorySummary(
    val id: Long,
    val name: String,
    val description: String?,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val htmlUrl: String,
)
