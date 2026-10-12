package com.test.kmp.octoboard.feature.repodetail.entities

internal data class GithubRepositoryLanguageBreakdown(
    val bytesByLanguage: Map<String, Long>,
)
