package com.podforeve.tracker.android

import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val chuckerModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(
                ChuckerInterceptor.Builder(androidContext())
                    .alwaysReadResponseBody(true)
                    .build(),
            )
            .build()
    }
}
