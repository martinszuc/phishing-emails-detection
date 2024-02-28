package com.martinszuc.phishing_emails_detection.data.email.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.martinszuc.phishing_emails_detection.data.email.local.db.dao.EmailBlobDao
import com.martinszuc.phishing_emails_detection.data.email.local.db.dao.EmailFullDao
import com.martinszuc.phishing_emails_detection.data.email.local.db.dao.EmailMinimalDao
import com.martinszuc.phishing_emails_detection.data.email.local.db.dao.SubjectDao
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.Subject
import com.martinszuc.phishing_emails_detection.utils.Converters

@Database(entities = [EmailMinimal::class, EmailFull::class, EmailBlob::class, Subject::class], exportSchema = false, version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailMinimalDao(): EmailMinimalDao
    abstract fun emailFullDao(): EmailFullDao
    abstract fun emailBlobDao(): EmailBlobDao
    abstract fun subjectDao(): SubjectDao

}

// TODO add userid column and add google id to user