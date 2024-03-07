package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.martinszuc.phishing_emails_detection.data.auth.AccountManager
import com.martinszuc.phishing_emails_detection.data.auth.UserRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageManager
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.email_package.PackageManifestManager
import com.martinszuc.phishing_emails_detection.data.file.FileManager
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.model.Model
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
     * @return An instance of [Model].
     */
    @Provides
    @Singleton
    fun provideClassifier(@ApplicationContext context: Context): Model {
        return Model(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): UserRepository =
        UserRepository(context)


    @Provides
    @Singleton
    fun provideGmailApiService(
        @ApplicationContext context: Context,
        accountManager: AccountManager
    ): GmailApiService =
        GmailApiService(context)

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Providing FileManager
    @Provides
    @Singleton
    fun provideFileManager(@ApplicationContext context: Context): FileManager {
        return FileManager(context)
    }

    // Providing FileRepository with FileManager dependency
    @Provides
    @Singleton
    fun provideFileRepository(fileManager: FileManager): FileRepository {
        return FileRepository(fileManager)
    }

    // Provide EmailBlobLocalRepository
    @Provides
    @Singleton
    fun provideEmailBlobLocalRepository(appDatabase: AppDatabase): EmailBlobLocalRepository {
        return EmailBlobLocalRepository(appDatabase)
    }

    // Provide PackageManifestManager
    @Provides
    @Singleton
    fun providePackageManifestManager(@ApplicationContext context: Context): PackageManifestManager {
        return PackageManifestManager(context)
    }

    // Provide EmailPackageManager
    @Provides
    @Singleton
    fun provideEmailPackageManager(
        emailBlobLocalRepository: EmailBlobLocalRepository,
        fileRepository: FileRepository,
        packageManifestManager: PackageManifestManager
    ): EmailPackageManager {
        return EmailPackageManager(emailBlobLocalRepository, fileRepository, packageManifestManager)
    }

    // Provide EmailPackageRepository
    @Provides
    @Singleton
    fun provideEmailPackageRepository(
        emailPackageManager: EmailPackageManager,
        packageManifestManager: PackageManifestManager,
        fileRepository: FileRepository
    ): EmailPackageRepository {
        return EmailPackageRepository(emailPackageManager, packageManifestManager, fileRepository)
    }

}
