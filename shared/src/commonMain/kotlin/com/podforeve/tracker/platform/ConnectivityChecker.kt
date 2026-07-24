package com.podforeve.tracker.platform

// Synchronous "do we have a usable network path right now" check.
// Android: ConnectivityManager.activeNetwork capabilities.
// iOS: SCNetworkReachability flags for esi.evetech.net.
expect class ConnectivityChecker {
    fun isOnline(): Boolean
}
