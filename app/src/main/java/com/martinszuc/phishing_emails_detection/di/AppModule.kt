package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.martinszuc.phishing_emails_detection.data.auth.AccountManager
import com.martinszuc.phishing_emails_detection.data.auth.UserRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import com.martinszuc.phishing_emails_detection.data.model.Classifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for providing app-level dependencies.
 *
 * This module includes providers for database and classifier dependencies.
 * It is installed in [SingletonComponent] to ensure that the provided instances are singletons
 * and live as long as the application does.
 *
 * @author Authored by matoszuc@gmail.com
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the application's Room database.
     *
     * @param context The application context.
     * @return The singleton instance of [AppDatabase].
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the classifier for the application.
     *
     * @param context The application context.
     * @return An instance of [Classifier].
     */
    @Provides
    @Singleton
    fun provideClassifier(@ApplicationContext context: Context): Classifier {
        return Classifier(context)
    }
    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): UserRepository =
        UserRepository(context)

    @Provides
    @Singleton
    fun provideGmailApiService(@ApplicationContext context: Context, accountManager: AccountManager): GmailApiService =
        GmailApiService(context)

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

}
