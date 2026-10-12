package com.test.kmp.octoboard.shared.entities

data class GithubRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val description: String?,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val openIssues: Int,
    val defaultBranch: String,
    val htmlUrl: String,
)
