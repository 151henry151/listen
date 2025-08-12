package com.listen.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the Listen app
 */
@Database(
    entities = [Segment::class],
    version = 1,
    exportSchema = false
)
abstract class ListenDatabase : RoomDatabase() {
    
    abstract fun segmentDao(): SegmentDao
    
    companion object {
        @Volatile
        private var INSTANCE: ListenDatabase? = null
        
        fun getDatabase(context: Context): ListenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ListenDatabase::class.java,
                    "listen_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 