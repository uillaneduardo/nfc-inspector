package com.nfcinspector.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TagEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from Database Schema v1 to v2:
         * Adds scanUuid for global sync deduplication and reader source metadata columns.
         * Existing records are preserved with default ANDROID_NFC reader metadata.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scanned_tags ADD COLUMN scanUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE scanned_tags ADD COLUMN readerSourceType TEXT NOT NULL DEFAULT 'ANDROID_NFC'")
                db.execSQL("ALTER TABLE scanned_tags ADD COLUMN readerName TEXT NOT NULL DEFAULT 'NFC Interno Android'")
                db.execSQL("ALTER TABLE scanned_tags ADD COLUMN readerId TEXT NOT NULL DEFAULT 'internal_android_adapter'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nfc_inspector_local.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

