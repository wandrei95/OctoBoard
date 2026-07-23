package com.test.kmp.octoboard.core.network

data class RateLimitInfo(
    val remaining: Int,
    val resetEpochSeconds: Long
) {
    companion object {
        val UNKNOWN = RateLimitInfo(remaining = -1, resetEpochSeconds = -1)
    }
}
