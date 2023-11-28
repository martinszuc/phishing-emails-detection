package com.martinszuc.phishing_emails_detection.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.martinszuc.phishing_emails_detection.data.local.db.dao.EmailsImportDao
import com.martinszuc.phishing_emails_detection.data.local.entity.Email

@Database(entities = [Email::class], exportSchema = false, version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailsImportDao(): EmailsImportDao
}
