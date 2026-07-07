package com.podforeve.tracker

import com.podforeve.tracker.di.platformModule
import com.podforeve.tracker.di.sharedModule
import com.podforeve.tracker.di.uiModule
import org.koin.core.context.startKoin

// Full Koin initialization for iOS. Called from Swift iOSApp.init().
// Replaces the partial init that was previously in shared/iosMain.
// Includes sharedModule (data/auth), platformModule (SecureStorage + driver), uiModule (ViewModels).
fun initKoin() {
    startKoin {
        modules(sharedModule, platformModule, uiModule)
    }
}
