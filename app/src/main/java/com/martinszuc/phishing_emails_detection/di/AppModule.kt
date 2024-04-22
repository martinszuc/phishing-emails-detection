package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.martinszuc.phishing_emails_detection.data.data_repository.auth.UserRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailDetectionLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.gmail_api.GmailApiService
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package.EmailPackageManifestManager
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileManager
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.python.model.DataProcessing
import com.martinszuc.phishing_emails_detection.data.python.model.Prediction
import com.martinszuc.phishing_emails_detection.data.python.model.Retraining
import com.martinszuc.phishing_emails_detection.data.python.model.Training
import com.martinszuc.phishing_emails_detection.data.python.model.WeightManager
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.model_manager.ModelManifestManager
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.network.retrofit.ModelWeightsService
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.processed_packages.ProcessedPackageManifestManager
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.processed_packages.ProcessedPackageRepository
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
 * @author matoszuc@gmail.com
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the classifier for the application.
     *
     * @return An instance of [Prediction].
     */
    @Provides
    @Singleton
    fun provideClassifier(): Prediction {
        return Prediction()
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): UserRepository =
        UserRepository(context)


    @Provides
    @Singleton
    fun provideGmailApiService(
        @ApplicationContext context: Context,
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
        return EmailPackageRepository(
            emailMboxLocalRepository,
            fileRepository,
            emailPackageManifestManager
        )
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
        return ModelRepository(
            modelManifestManager,
            fileRepository,
            modelWeightsService,
            weightManager
        )
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

    @Provides
    @Singleton
    fun provideEmailDetectionLocalRepository(
        database: AppDatabase,
        fileRepository: FileRepository,
        emailMboxLocalRepository: EmailMboxLocalRepository
    ): EmailDetectionLocalRepository {
        return EmailDetectionLocalRepository(database, fileRepository, emailMboxLocalRepository)
    }
}
