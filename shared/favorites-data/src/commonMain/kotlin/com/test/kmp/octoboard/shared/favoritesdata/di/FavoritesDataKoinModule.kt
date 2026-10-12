package com.test.kmp.octoboard.shared.favoritesdata.di

import com.test.kmp.octoboard.core.database.DatabaseDriverFactory
import com.test.kmp.octoboard.shared.favoritesdata.FavoritesStore
import com.test.kmp.octoboard.shared.favoritesdata.local.FavoritesDatabase
import org.koin.dsl.module

val favoritesDataKoinModule = module {
    single<FavoritesDatabase> {
        FavoritesDatabase(
            driver = get<DatabaseDriverFactory>().createDriver(
                schema = FavoritesDatabase.Schema,
                dbName = "favorites.db",
            ),
        )
    }
    single<FavoritesStore> { FavoritesStore(database = get<FavoritesDatabase>()) }
}
