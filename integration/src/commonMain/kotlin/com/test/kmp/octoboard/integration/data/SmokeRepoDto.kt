package com.test.kmp.octoboard.integration.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SmokeRepoDto(
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    @SerialName("stargazers_count") val stargazersCount: Int = 0
)
