package com.podforeve.tracker.platform

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

private const val POLL_INTERVAL_MILLIS = 5_000L

// Deliberately polling rather than a native push-based observer (ConnectivityManager
// NetworkCallback / NWPathMonitor): a single ConnectivityChecker.isOnline() call is already
// symmetric across both platforms, so polling avoids writing (and maintaining) two different
// callback-registration lifecycles for a "you're offline" banner that only needs to react within
// a few seconds, not instantly.
class ConnectivityObserver(private val checker: ConnectivityChecker) {
    val isOnline: Flow<Boolean> = flow {
        while (true) {
            emit(checker.isOnline())
            delay(POLL_INTERVAL_MILLIS)
        }
    }.distinctUntilChanged()
}
