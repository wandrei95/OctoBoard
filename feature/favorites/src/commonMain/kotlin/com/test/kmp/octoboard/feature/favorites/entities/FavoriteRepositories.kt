package com.test.kmp.octoboard.feature.favorites.entities

import com.test.kmp.octoboard.core.common.entities.GithubRepository

/**
 * Snapshot of the user's favorites. [failedCount] is the number of saved ids whose repository
 * couldn't be loaded (deleted, private, rate limited, offline...), so a partial list can still
 * be shown.
 */
data class FavoriteRepositories(
    val repos: List<GithubRepository>,
    val failedCount: Int,
)
