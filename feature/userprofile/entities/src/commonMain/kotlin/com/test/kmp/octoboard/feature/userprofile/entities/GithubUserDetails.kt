package com.test.kmp.octoboard.feature.userprofile.entities

data class GithubUserDetails(
    val login: String,
    val avatarUrl: String?,
    val name: String?,
    val bio: String?,
    val company: String?,
    val location: String?,
    val blogUrl: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val htmlUrl: String,
)
