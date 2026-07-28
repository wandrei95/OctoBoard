package com.test.kmp.octoboard.core.network

interface AuthTokenProvider {
    suspend fun getToken(): String?
}
