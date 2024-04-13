package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.martinszuc.phishing_emails_detection.data.auth.AccountManager
import com.martinszuc.phishing_emails_detection.data.auth.UserRepository
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageManifestManager
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.file.FileManager
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.model.DataProcessing
import com.martinszuc.phishing_emails_detection.data.model.Prediction
import com.martinszuc.phishing_emails_detection.data.model.Retraining
import com.martinszuc.phishing_emails_detection.data.model.Training
import com.martinszuc.phishing_emails_detection.data.model.WeightManager
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelManifestManager
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.model_manager.retrofit.ModelWeightsService
import com.martinszuc.phishing_emails_detection.data.processed_packages.ProcessedPackageManifestManager
import com.martinszuc.phishing_emails_detection.data.processed_packages.ProcessedPackageRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
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
     * Provides the classifier for the application.
     *
     * @param context The application context.
     * @return An instance of [Prediction].
     */
    @Provides
    @Singleton
    fun provideClassifier(@ApplicationContext context: Context): Prediction {
        return Prediction(context)
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

    // Provide EmailBlobLocalRepository
    @Provides
    @Singleton
    fun provideEmailMboxLocalRepository(
        appDatabase: AppDatabase,
        fileRepository: FileRepository
    ): EmailMboxLocalRepository {
        return EmailMboxLocalRepository(appDatabase, fileRepository)
    }

    // Provide PackageManifestManager
    @Provides
    @Singleton
    fun providePackageManifestManager(@ApplicationContext context: Context): EmailPackageManifestManager {
        return EmailPackageManifestManager(context)
    }

    // Provide EmailPackageRepository
    @Provides
    @Singleton
    fun provideEmailPackageRepository(
        emailMboxLocalRepository: EmailMboxLocalRepository,
        emailPackageManifestManager: EmailPackageManifestManager,
        fileRepository: FileRepository
    ): EmailPackageRepository {
        return EmailPackageRepository(emailMboxLocalRepository, fileRepository, emailPackageManifestManager)
    }

    // Provide MachineLearningUtils
    @Provides
    @Singleton
    fun provideMachineLearningUtils(): DataProcessing {
        // You may need to adjust the parameters according to the constructor of MachineLearningUtils
        return DataProcessing()
    }

    @Provides
    @Singleton
    fun provideProcessedPackageManifestManager(@ApplicationContext context: Context): ProcessedPackageManifestManager {
        return ProcessedPackageManifestManager(context)
    }

    @Provides
    @Singleton
    fun provideProcessedPackageRepository(
        processedPackageManifestManager: ProcessedPackageManifestManager,
        fileRepository: FileRepository
    ): ProcessedPackageRepository {
        return ProcessedPackageRepository(processedPackageManifestManager, fileRepository)
    }

    @Provides
    @Singleton
    fun provideModelManifestManager(@ApplicationContext context: Context): ModelManifestManager {
        return ModelManifestManager(context)
    }

    @Provides
    @Singleton
    fun provideModelRepository(
        modelManifestManager: ModelManifestManager,
        fileRepository: FileRepository,
        modelWeightsService: ModelWeightsService, // Add this parameter
        weightManager: WeightManager // Ensure this parameter is here if needed by ModelRepository
    ): ModelRepository {
        return ModelRepository(modelManifestManager, fileRepository, modelWeightsService, weightManager)
    }

    // If your Training class requires any dependencies, provide them here
    @Provides
    @Singleton
    fun provideTraining(): Training {
        // Adjust the Training class constructor as necessary
        return Training()
    }

    @Provides
    @Singleton
    fun provideRetraining(): Retraining {
        return Retraining()
    }

    @Provides
    @Singleton
    fun provideWeightManager(): WeightManager {
        return WeightManager()
    }

}
