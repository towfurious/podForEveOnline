package com.podforeve.tracker.di

import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.data.db.DatabaseDriverFactory
import com.podforeve.tracker.platform.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val platformModule = module {
    single { SecureStorage() }
    single { DatabaseDriverFactory() }

    val ssoNamed = named("sso")
    val esiNamed = named("esi")

    single(ssoNamed) {
        HttpClient(Darwin) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(Logging) { level = LogLevel.HEADERS }
        }
    }

    single(esiNamed) {
        val authRepo = get<AuthRepository>()
        HttpClient(Darwin) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(Logging) { level = LogLevel.HEADERS }
            install(Auth) {
                bearer {
                    loadTokens {
                        authRepo.getValidAccessToken()?.let { BearerTokens(it, "") }
                    }
                    refreshTokens {
                        authRepo.getValidAccessToken()?.let { BearerTokens(it, "") }
                    }
                    sendWithoutRequest { it.url.host == "esi.evetech.net" }
                }
            }
        }
    }
}
