package com.podforeve.tracker

import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor2.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

// Called from Swift to embed Compose into UIKit.
fun MainViewController() = ComposeUIViewController { App() }

// Configure Coil to use a Ktor/Darwin HTTP client for network image loading on iOS.
// Called once after Koin init, before the first Compose frame renders.
fun initCoil() {
    val httpClient = HttpClient(Darwin)
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(it)
            .components { add(KtorNetworkFetcherFactory(httpClient = httpClient)) }
            .build()
    }
}
