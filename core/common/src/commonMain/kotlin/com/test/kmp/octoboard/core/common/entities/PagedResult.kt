package com.test.kmp.octoboard.core.common.entities

data class PagedResult<T>(
    val items: List<T>,
    val hasNextPage: Boolean,
)
