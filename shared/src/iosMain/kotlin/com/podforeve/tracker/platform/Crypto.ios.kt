package com.podforeve.tracker.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual fun sha256(data: ByteArray): ByteArray {
    val output = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    data.usePinned { inputPinned ->
        output.usePinned { outputPinned ->
            CC_SHA256(inputPinned.addressOf(0), data.size.toUInt(), outputPinned.addressOf(0).reinterpret())
        }
    }
    return output
}

@OptIn(ExperimentalForeignApi::class)
actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        val result = SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        check(result == errSecSuccess) { "SecRandomCopyBytes failed with status $result" }
    }
    return bytes
}
