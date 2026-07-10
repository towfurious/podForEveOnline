package com.podforeve.tracker.di

import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.EsiAuthService
import com.podforeve.tracker.auth.OAuthPkceHelper
import com.podforeve.tracker.data.db.DatabaseDriverFactory
import com.podforeve.tracker.data.remote.esi.CharacterEsiApi
import com.podforeve.tracker.data.remote.esi.IndustryJobEsiApi
import com.podforeve.tracker.data.remote.esi.PlanetEsiApi
import com.podforeve.tracker.data.remote.esi.SkillQueueEsiApi
import com.podforeve.tracker.data.repository.CharacterRepository
import com.podforeve.tracker.data.repository.IndustryJobRepository
import com.podforeve.tracker.data.repository.PlanetRepository
import com.podforeve.tracker.data.repository.SkillQueueRepository
import com.podforeve.tracker.db.AppDatabase
import org.koin.core.qualifier.named
import org.koin.dsl.module

// HTTP clients (named "sso" / "esi") are provided by platformModule
// so they can use platform-specific engines (OkHttp+Chucker on Android, Darwin on iOS).
val sharedModule = module {
    val ssoClient = named("sso")
    val esiClient = named("esi")

    single { OAuthPkceHelper() }
    single { EsiAuthService(get(ssoClient)) }
    single { AuthRepository(get(), get(), get()) }
    single { AppDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single {
        SkillQueueEsiApi(
            esiClient = get(esiClient),
            publicClient = get(ssoClient),
        )
    }
    single { SkillQueueRepository(get(), get()) }
    single {
        CharacterEsiApi(
            esiClient = get(esiClient),
            publicClient = get(ssoClient),
        )
    }
    single { CharacterRepository(get(), get()) }
    single {
        PlanetEsiApi(
            esiClient = get(esiClient),
            publicClient = get(ssoClient),
        )
    }
    single { PlanetRepository(get(), get()) }
    single {
        IndustryJobEsiApi(
            esiClient = get(esiClient),
            publicClient = get(ssoClient),
        )
    }
    single { IndustryJobRepository(get(), get()) }
}
