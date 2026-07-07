package com.podforeve.tracker.android

import android.app.Application
import com.podforeve.tracker.di.platformModule
import com.podforeve.tracker.di.sharedModule
import com.podforeve.tracker.di.uiModule
import com.podforeve.tracker.platform.AppContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PodForEveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.instance = this
        startKoin {
            androidContext(this@PodForEveApplication)
            modules(sharedModule, platformModule, uiModule, chuckerModule)
        }
    }
}
