package com.test.kmp.octoboard.core.network

interface AuthTokenProvider {
    fun getToken(): String?
}
