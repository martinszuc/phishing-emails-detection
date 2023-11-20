package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import androidx.room.Room
import com.martinszuc.phishing_emails_detection.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "phishing-emails-detection-database"  // TODO this string should be declared somewhere imo
        ).build()
    }
}