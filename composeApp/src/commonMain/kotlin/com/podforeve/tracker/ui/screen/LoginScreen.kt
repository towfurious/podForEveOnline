package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.platform.rememberUrlLauncher
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun LoginScreen() {
    val authRepository: AuthRepository = remember { getKoin().get() }
    val authState by authRepository.authState.collectAsState()
    val urlLauncher = rememberUrlLauncher()
    val scope = rememberCoroutineScope()

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "PodForEve",
                style = MaterialTheme.typography.displayMedium,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your EVE Online companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(Modifier.size(48.dp))
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            val url = authRepository.startLogin()
                            urlLauncher(url)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Login with EVE Online")
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
