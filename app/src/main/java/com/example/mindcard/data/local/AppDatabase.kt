package com.example.mindcard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mindcard.data.local.dao.CardDao
import com.example.mindcard.data.local.dao.DeckDao
import com.example.mindcard.data.local.dao.UserProfileDao
import com.example.mindcard.data.local.entity.CardEntity
import com.example.mindcard.data.local.entity.DeckEntity
import com.example.mindcard.data.local.entity.UserProfileEntity

@Database(
    entities = [
        CardEntity::class,
        DeckEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindcard_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
