package com.batodev.arrows.data.di

import androidx.room.Room
import com.batodev.arrows.data.AppDatabase
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.data.MIGRATION_1_2
import com.batodev.arrows.data.UserPreferencesEntity
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.data.migrateFromDataStoreIfNeeded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), AppDatabase::class.java, "arrows_db")
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
        }
        single {
            val dao = get<AppDatabase>().corePreferencesDao()
            CoroutineScope(Dispatchers.IO).launch {
                migrateFromDataStoreIfNeeded(androidContext(), dao)
                dao.insertDefault(UserPreferencesEntity())
            }
            dao
        }
        single { get<AppDatabase>().gameplayPreferencesDao() }
        single { get<AppDatabase>().debugPreferencesDao() }
        single { get<AppDatabase>().monetizationPreferencesDao() }
        single { get<AppDatabase>().gameStateDao() }
        single { UserPreferencesRepository(get(), get(), get(), get()) }
        single<IUserPreferencesRepository> { get<UserPreferencesRepository>() }
    }
