package com.podforeve.tracker.data.db

import app.cash.sqldelight.db.SqlDriver

// expect/actual: creates the correct SQLite driver per platform.
// See wiki: [[ADR-004 - Ktor SQLDelight Koin Coil3 Stack]], [[SQLDelight Migrations]]
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
