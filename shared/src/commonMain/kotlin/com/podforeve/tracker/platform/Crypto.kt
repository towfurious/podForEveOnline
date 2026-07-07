package com.podforeve.tracker.platform

// Platform-specific primitives needed for PKCE. See wiki: [[OAuth2 PKCE]]
expect fun sha256(data: ByteArray): ByteArray
expect fun secureRandomBytes(size: Int): ByteArray
