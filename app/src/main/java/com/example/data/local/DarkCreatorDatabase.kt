package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ContentEntity::class, GeneratedCreationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DarkCreatorDatabase : RoomDatabase() {
    abstract fun dao(): DarkCreatorDao

    companion object {
        @Volatile
        private var INSTANCE: DarkCreatorDatabase? = null

        fun getDatabase(context: Context): DarkCreatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DarkCreatorDatabase::class.java,
                    "dark_creator_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
