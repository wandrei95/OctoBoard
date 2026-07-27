package com.test.kmp.octoboard.feature.search.entities

data class GithubRepositorySummary(
    val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val description: String?,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val htmlUrl: String,
)
