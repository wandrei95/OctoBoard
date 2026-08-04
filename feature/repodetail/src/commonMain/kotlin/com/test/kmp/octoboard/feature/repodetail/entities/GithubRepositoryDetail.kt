package com.test.kmp.octoboard.feature.repodetail.entities

import com.test.kmp.octoboard.core.common.entities.GithubRepository

data class GithubRepositoryDetail(
    val repository: GithubRepository,
    val isFavorite: Boolean,
)
