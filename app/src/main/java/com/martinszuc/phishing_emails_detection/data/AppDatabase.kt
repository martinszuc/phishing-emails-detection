package com.martinszuc.phishing_emails_detection.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.martinszuc.phishing_emails_detection.data.dao.EmailDao
import com.martinszuc.phishing_emails_detection.data.entity.Email

@Database(entities = [Email::class], exportSchema = false, version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "phishing-emails-detection-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
