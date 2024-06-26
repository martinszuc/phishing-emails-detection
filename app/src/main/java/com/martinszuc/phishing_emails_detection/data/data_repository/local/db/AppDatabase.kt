package com.martinszuc.phishing_emails_detection.data.data_repository.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.dao.EmailDetectionDao
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.dao.EmailFullDao
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.dao.EmailMboxMetadataDao
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.dao.EmailMinimalDao
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.dao.SubjectDao
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMboxMetadata
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.Subject
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailDetection
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.utils.Converters

/**
 * @author matoszuc@gmail.com
 */

@Database(entities = [EmailMinimal::class, EmailFull::class, EmailMboxMetadata::class, Subject::class, EmailDetection::class], exportSchema = false, version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailMinimalDao(): EmailMinimalDao
    abstract fun emailFullDao(): EmailFullDao
    abstract fun emailMboxDao(): EmailMboxMetadataDao
    abstract fun subjectDao(): SubjectDao
    abstract fun emailDetectionDao(): EmailDetectionDao

}

// TODO add userid column and add google id to user