package com.podforeve.tracker.platform

import android.content.Context

// Holds the Application context so expect-class actuals that need Context can access it
// without constructor parameters (expect/actual constructors must match across platforms).
// Set once in PodForEveApplication.onCreate() before any SecureStorage call.
object AppContext {
    lateinit var instance: Context
}
