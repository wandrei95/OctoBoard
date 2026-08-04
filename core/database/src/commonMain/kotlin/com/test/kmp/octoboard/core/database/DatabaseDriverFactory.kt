package com.test.kmp.octoboard.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

expect class DatabaseDriverFactory {
    fun createDriver(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        dbName: String,
    ): SqlDriver
}
