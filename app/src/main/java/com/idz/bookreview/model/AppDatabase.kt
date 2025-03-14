package com.idz.bookreview.model

import android.content.Context
import androidx.room.*
import com.idz.bookreview.model.dao.ReviewDao

@Database(entities = [Review::class], version = 5, exportSchema = false) // ✅ שימי לב לגרסה המעודכנת
abstract class AppDatabase : RoomDatabase() {

    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_review_database"
                ).fallbackToDestructiveMigration() // ✅ מאפשר מחיקה אם יש שינוי בגרסה
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
