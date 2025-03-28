package com.idz.bookreview.model.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.idz.bookreview.model.Converters
import com.idz.bookreview.model.User
import com.idz.bookreview.model.Review

@Database(entities = [User::class, Review::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
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
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE reviews ADD COLUMN likes TEXT NOT NULL DEFAULT '[]'")
                } catch (e: Exception) {
                    println("Column 'likes' already exists, skipping migration.")
                }
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE reviews RENAME TO reviews_old;")

                    db.execSQL("""
                        CREATE TABLE reviews (
                            id TEXT PRIMARY KEY NOT NULL,
                            userId TEXT NOT NULL,
                            userName TEXT NOT NULL,
                            title TEXT NOT NULL,
                            author TEXT NOT NULL,
                            review TEXT NOT NULL,
                            imageUrl TEXT,
                            timestamp INTEGER NOT NULL,
                            favoritedByUsers TEXT NOT NULL DEFAULT '[]'
                        )
                    """.trimIndent())

                    db.execSQL("""
                        INSERT INTO reviews (id, userId, userName, title, author, review, imageUrl, timestamp, favoritedByUsers)
                        SELECT id, userId, userName, title, author, review, imageUrl, timestamp, '[]'
                        FROM reviews_old
                    """.trimIndent())

                    db.execSQL("DROP TABLE reviews_old;")
                } catch (e: Exception) {
                    println("Error during migration from version 8 to 9: ${e.message}")
                }
            }
        }
    }
}
