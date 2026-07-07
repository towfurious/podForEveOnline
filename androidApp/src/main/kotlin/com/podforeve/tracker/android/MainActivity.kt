package com.podforeve.tracker.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.podforeve.tracker.App
import com.podforeve.tracker.auth.AuthRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }

    // Called when Chrome Custom Tabs delivers eveauth-podforeve://callback back to this Activity.
    // The intent-filter in AndroidManifest.xml routes the URI here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val callbackUrl = intent.data?.toString() ?: return
        if (callbackUrl.startsWith("eveauth-podforeve://callback")) {
            lifecycleScope.launch {
                authRepository.completeLogin(callbackUrl)
            }
        }
    }
}
