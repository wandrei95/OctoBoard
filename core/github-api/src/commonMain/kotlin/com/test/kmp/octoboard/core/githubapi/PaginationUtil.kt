package com.test.kmp.octoboard.core.githubapi

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

fun Headers.hasNextPage(): Boolean = this[HttpHeaders.Link]?.contains("rel=\"next\"") == true
