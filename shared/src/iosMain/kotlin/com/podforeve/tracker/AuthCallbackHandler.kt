package com.podforeve.tracker

import com.podforeve.tracker.auth.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin

// Coroutine scope for iOS — lives for the app lifetime.
private val iosAppScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

// Called from Swift iOSApp.body onOpenURL — bridges the OAuth callback URL into Kotlin.
fun handleAuthCallback(url: String) {
    val authRepository = getKoin().get<AuthRepository>()
    iosAppScope.launch {
        authRepository.completeLogin(url)
    }
}
