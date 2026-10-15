package com.test.kmp.octoboard.feature.search.data.datasources

import com.test.kmp.octoboard.feature.search.entities.SearchSort

internal fun buildRepoSearchQualifiers(
    query: String,
    language: String?,
): String =
    if (language.isNullOrBlank()) {
        query
    } else {
        "$query language:$language"
    }

internal fun SearchSort.toSortParams(): Map<String, String> = when (this) {
    SearchSort.BestMatch -> emptyMap()
    SearchSort.Stars -> mapOf(
        "sort" to "stars",
        "order" to "desc",
    )
    SearchSort.Forks -> mapOf(
        "sort" to "forks",
        "order" to "desc",
    )
    SearchSort.Updated -> mapOf(
        "sort" to "updated",
        "order" to "desc",
    )
}
