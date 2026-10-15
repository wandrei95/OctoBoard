package com.test.kmp.octoboard.feature.auth.data

import com.test.kmp.octoboard.core.network.AuthTokenProvider

expect class SecureStorage : AuthTokenProvider {
    override suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
