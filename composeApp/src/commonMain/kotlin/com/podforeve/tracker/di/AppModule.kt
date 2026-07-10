package com.podforeve.tracker.di

import com.podforeve.tracker.ui.theme.ThemeRepository
import com.podforeve.tracker.ui.viewmodel.DashboardViewModel
import com.podforeve.tracker.ui.viewmodel.IndustryJobViewModel
import com.podforeve.tracker.ui.viewmodel.PlanetViewModel
import com.podforeve.tracker.ui.viewmodel.SkillQueueViewModel
import org.koin.dsl.module

// Koin module for the UI layer (composeApp).
// Kept separate from sharedModule so shared has no dependency on Compose / Voyager.
val uiModule = module {
    single { ThemeRepository(get()) }
    factory { SkillQueueViewModel(get(), get()) }
    factory { DashboardViewModel(get(), get(), get()) }
    factory { PlanetViewModel(get(), get()) }
    factory { IndustryJobViewModel(get(), get()) }
}
