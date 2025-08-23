package com.listen.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for the Listen app
 */
@Database(
    entities = [Segment::class],
    version = 4,
    exportSchema = true
)
abstract class ListenDatabase : RoomDatabase() {
    
    abstract fun segmentDao(): SegmentDao
    
    companion object {
        @Volatile
        private var INSTANCE: ListenDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op migration placeholder. Schema unchanged; kept for future additions.
                // Example: database.execSQL("ALTER TABLE segments ADD COLUMN example INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE segments ADD COLUMN isPhoneCall INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE segments ADD COLUMN callDirection TEXT")
                database.execSQL("ALTER TABLE segments ADD COLUMN phoneNumber TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE segments ADD COLUMN isSavedToDownloads INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): ListenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ListenDatabase::class.java,
                    "listen_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration() // Add fallback for migration failures
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 