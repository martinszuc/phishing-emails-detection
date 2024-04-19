package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * Provides the application's Room database.
     *
     * @param context The application context.
     * @return The singleton instance of [AppDatabase].
     */
    @Provides
    @Singleton
    fun provideOpenHelperFactory(): SupportSQLiteOpenHelper.Factory {
        return object : SupportSQLiteOpenHelper.Factory {
            override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
                val frameworkFactory = FrameworkSQLiteOpenHelperFactory()
                val newConfiguration = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
                    .name(configuration.name)
                    .callback(configuration.callback)
                    // Room does not expose direct cursor window customization; hence it's omitted.
                    .build()
                return frameworkFactory.create(newConfiguration)
            }
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        openHelperFactory: SupportSQLiteOpenHelper.Factory
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "your-database-name")
            .openHelperFactory(openHelperFactory)
            .fallbackToDestructiveMigration()
            .build()
    }
}