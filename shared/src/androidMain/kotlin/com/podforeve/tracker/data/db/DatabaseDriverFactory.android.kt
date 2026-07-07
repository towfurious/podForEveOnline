package com.podforeve.tracker.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.platform.AppContext

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = AppDatabase.Schema,
        context = AppContext.instance,
        name = "app_database.db",
    )
}
