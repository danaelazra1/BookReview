package com.idz.bookreview.model.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.idz.bookreview.model.User
import com.idz.bookreview.model.Review

@Database(entities = [User::class, Review::class], version = 5, exportSchema = false)
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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE reviews ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE reviews ADD COLUMN imageUrl TEXT")
                } catch (e: Exception) {
                    println("Columns already exist, skipping migration.")
                }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE reviews ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE reviews ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE reviews ADD COLUMN imageUrl TEXT")
                } catch (e: Exception) {
                    println("Columns already exist, skipping migration.")
                }
            }
        }
    }
}
