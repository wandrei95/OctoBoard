package com.test.kmp.octoboard.feature.repodetail.entities

data class GithubRepositoryLanguageBreakdown(
    val bytesByLanguage: Map<String, Long>,
)
