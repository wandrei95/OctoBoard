package com.test.kmp.octoboard.shared.githubapi

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

fun Headers.hasNextPage(): Boolean = this[HttpHeaders.Link]?.contains("rel=\"next\"") == true
