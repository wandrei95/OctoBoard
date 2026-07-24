package com.test.kmp.octoboard.feature.search.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserSearchResponseDto(
    val items: List<UserItemDto> = emptyList(),
)

@Serializable
internal data class UserItemDto(
    val id: Long,
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
)
