package com.podforeve.tracker.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable

actual class ConnectivityChecker {
    @OptIn(ExperimentalForeignApi::class)
    actual fun isOnline(): Boolean = memScoped {
        val reachability = SCNetworkReachabilityCreateWithName(null, "esi.evetech.net") ?: return false
        val flags = alloc<SCNetworkReachabilityFlagsVar>()
        if (!SCNetworkReachabilityGetFlags(reachability, flags.ptr)) return false

        val reachable = flags.value.toInt() and kSCNetworkReachabilityFlagsReachable.toInt() != 0
        val connectionRequired = flags.value.toInt() and kSCNetworkReachabilityFlagsConnectionRequired.toInt() != 0
        reachable && !connectionRequired
    }
}
