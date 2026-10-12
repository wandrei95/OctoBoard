package com.test.kmp.octoboard.core.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

sealed class ApiError(
    message: String,
    cause: Throwable? = null,
) : Exception(
    // Positional, not named: Exception resolves to java.lang.Exception on JVM/Android targets,
    // and Kotlin prohibits named arguments for non-Kotlin (Java) functions.
    message,
    cause,
) {
    data class Network(val cause0: Throwable) : ApiError(
        message = "Network error",
        cause = cause0,
    )

    data class Timeout(val cause0: Throwable) : ApiError(
        message = "Request timed out",
        cause = cause0,
    )

    data class Http(
        val code: Int,
        val body: String?,
    ) : ApiError("HTTP $code")

    data class Serialization(val cause0: Throwable) : ApiError(
        message = "Failed to parse response",
        cause = cause0,
    )

    data class Unknown(val cause0: Throwable) : ApiError(
        message = "Unexpected error",
        cause = cause0,
    )
}

@OptIn(ExperimentalContracts::class)
@Suppress("WRONG_INVOCATION_KIND")
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        val res = block()
        Result.success(res)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: ClientRequestException) { // 4xx
        Result.failure(
            ApiError.Http(
                code = e.response.status.value,
                body = e.response.bodyAsTextSafe(),
            )
        )
    } catch (e: ServerResponseException) { // 5xx
        Result.failure(
            ApiError.Http(
                code = e.response.status.value,
                body = e.response.bodyAsTextSafe(),
            )
        )
    } catch (e: ResponseException) { // fallback for other HttpStatusCode errors
        Result.failure(
            ApiError.Http(
                code = e.response.status.value,
                body = e.response.bodyAsTextSafe(),
            )
        )
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiError.Timeout(e))
    } catch (e: ConnectTimeoutException) {
        Result.failure(ApiError.Timeout(e))
    } catch (e: SocketTimeoutException) {
        Result.failure(ApiError.Timeout(e))
    } catch (e: IOException) { // no internet, DNS failure, etc.
        Result.failure(ApiError.Network(e))
    } catch (e: SerializationException) {
        Result.failure(ApiError.Serialization(e))
    } catch (e: CancellationException) {
        throw e // NEVER swallow this — breaks structured concurrency
    } catch (e: Exception) {
        Result.failure(ApiError.Unknown(e))
    }
}

suspend fun HttpResponse.bodyAsTextSafe(): String? =
    runCatching { bodyAsText() }.getOrNull()
