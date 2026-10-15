package com.test.kmp.octoboard.feature.auth.data

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

// kSecClass/kSecAttr.../kSecReturn.../kSecMatch... constants are CFStringRefs; NSMutableDictionary
// keys need Kotlin/Native's ObjC `Any?`. CFBridgingRelease bridges a retained CFTypeRef to its
// toll-free-bridged Foundation counterpart (NSString here), which NSMutableDictionary can use directly.
@OptIn(ExperimentalForeignApi::class)
private fun cfStringKey(value: platform.CoreFoundation.CFStringRef?): NSString =
    CFBridgingRelease(value) as NSString

// NSMutableDictionary (an ObjC object) can't be `as`-cast directly to CFDictionaryRef (a raw C
// pointer typealias) - there's no subtype relationship between them in Kotlin's type system despite
// being toll-free bridged at the ObjC runtime level. interpretCPointer + objcPtr() views the same
// underlying object as a CFDictionaryRef without transferring ownership (no retain/release needed,
// since the NSMutableDictionary stays rooted by its own Kotlin reference for the call's duration).
@OptIn(ExperimentalForeignApi::class)
private fun NSMutableDictionary.asCFDictionary(): CFDictionaryRef? = interpretCPointer(objcPtr())

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    if (result.isNotEmpty()) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return result
}

@OptIn(ExperimentalForeignApi::class)
actual class SecureStorage : AuthTokenProvider {

    actual override suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        val query = baseQuery().apply {
            setObject(true, forKey = cfStringKey(kSecReturnData))
            setObject(cfStringKey(kSecMatchLimitOne), forKey = cfStringKey(kSecMatchLimit))
        }
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query.asCFDictionary(), result.ptr)
            if (status != errSecSuccess) {
                return@memScoped null
            }
            val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
            data.toByteArray().decodeToString()
        }
    }

    actual suspend fun saveToken(token: String) {
        clearToken()
        withContext(Dispatchers.IO) {
            val attributes = baseQuery().apply {
                setObject(token.encodeToByteArray().toNSData(), forKey = cfStringKey(kSecValueData))
            }
            SecItemAdd(attributes.asCFDictionary(), null)
        }
    }

    actual suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            SecItemDelete(baseQuery().asCFDictionary())
        }
    }

    private fun baseQuery(): NSMutableDictionary = NSMutableDictionary().apply {
        setObject(cfStringKey(kSecClassGenericPassword), forKey = cfStringKey(kSecClass))
        setObject(SERVICE, forKey = cfStringKey(kSecAttrService))
        setObject(ACCOUNT, forKey = cfStringKey(kSecAttrAccount))
    }

    private companion object {
        const val SERVICE = "com.test.kmp.octoboard.auth"
        const val ACCOUNT = "github_pat"
    }
}
