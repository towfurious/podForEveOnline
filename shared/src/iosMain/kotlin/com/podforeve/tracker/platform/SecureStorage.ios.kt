package com.podforeve.tracker.platform

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSData
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

// Security framework constants are CPointer<cnames.structs.__CFString> in KN 2.0 — not toll-free
// bridged to NSString — so we use CFDictionaryCreateMutable/CFDictionarySetValue (pure CF) which
// accept CValuesRef<*>? for both keys and values, avoiding the NSMutableDictionary type mismatch.
@OptIn(ExperimentalForeignApi::class)
actual class SecureStorage {
    private val serviceName = "com.podforeve.tracker"

    actual fun read(key: String): String? = memScoped {
        val dict = CFDictionaryCreateMutable(
            null,
            5L,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        ) ?: return@memScoped null

        val cfService = CFStringCreateWithCString(null, serviceName, kCFStringEncodingUTF8)
        val cfKey = CFStringCreateWithCString(null, key, kCFStringEncodingUTF8)

        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(dict, kSecAttrService, cfService)
        CFDictionarySetValue(dict, kSecAttrAccount, cfKey)
        CFDictionarySetValue(dict, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(dict, kSecMatchLimit, kSecMatchLimitOne)

        CFRelease(cfService)
        CFRelease(cfKey)

        val result = alloc<ObjCObjectVar<Any?>>()
        val status = SecItemCopyMatching(dict, result.ptr.reinterpret())
        CFRelease(dict)

        if (status == errSecSuccess) {
            (result.value as? NSData)?.let { data ->
                data.bytes?.reinterpret<ByteVar>()?.readBytes(data.length.toInt())?.decodeToString()
            }
        } else {
            null
        }
    }

    actual fun write(key: String, value: String) {
        delete(key)
        val encoded = value.encodeToByteArray()
        val cfData = encoded.usePinned { pinned ->
            CFDataCreate(null, pinned.addressOf(0).reinterpret(), encoded.size.toLong())
        } ?: return

        memScoped {
            val dict = CFDictionaryCreateMutable(
                null,
                5L,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr,
            ) ?: run {
                CFRelease(cfData)
                return@memScoped
            }

            val cfService = CFStringCreateWithCString(null, serviceName, kCFStringEncodingUTF8)
            val cfAccount = CFStringCreateWithCString(null, key, kCFStringEncodingUTF8)

            CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(dict, kSecAttrService, cfService)
            CFDictionarySetValue(dict, kSecAttrAccount, cfAccount)
            CFDictionarySetValue(dict, kSecValueData, cfData)
            CFDictionarySetValue(dict, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)

            SecItemAdd(dict, null)

            CFRelease(cfService)
            CFRelease(cfAccount)
            CFRelease(cfData)
            CFRelease(dict)
        }
    }

    actual fun delete(key: String) {
        memScoped {
            val dict = CFDictionaryCreateMutable(
                null,
                3L,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr,
            ) ?: return@memScoped

            val cfService = CFStringCreateWithCString(null, serviceName, kCFStringEncodingUTF8)
            val cfAccount = CFStringCreateWithCString(null, key, kCFStringEncodingUTF8)

            CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(dict, kSecAttrService, cfService)
            CFDictionarySetValue(dict, kSecAttrAccount, cfAccount)

            SecItemDelete(dict)

            CFRelease(cfService)
            CFRelease(cfAccount)
            CFRelease(dict)
        }
    }
}
