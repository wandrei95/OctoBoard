package com.test.kmp.octoboard.feature.repodetail.entities

data class GithubRepositoryDetails(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val defaultBranch: String,
    val htmlUrl: String,
)
