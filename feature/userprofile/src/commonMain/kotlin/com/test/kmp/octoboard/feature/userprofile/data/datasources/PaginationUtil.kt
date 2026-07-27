package com.test.kmp.octoboard.feature.userprofile.data.datasources

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

internal fun Headers.hasNextPage(): Boolean = this[HttpHeaders.Link]?.contains("rel=\"next\"") == true
