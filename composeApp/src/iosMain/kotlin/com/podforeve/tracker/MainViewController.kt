package com.podforeve.tracker

import androidx.compose.ui.window.ComposeUIViewController

// Called from Swift AppDelegate to embed Compose into UIKit.
fun MainViewController() = ComposeUIViewController { App() }
