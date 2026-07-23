package com.test.kmp.octoboard.integration.entities

// Throwaway phase-1 smoke-test entity — deleted once :feature:search owns its own domain models.
data class SmokeRepo(
    val fullName: String,
    val description: String?,
    val stargazersCount: Int
)
