package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String = "", // ✅ מזהה ייחודי של הביקורת (Firebase יצור UUID)
    val bookTitle: String = "", // ✅ שם הספר שעליו נכתבה הביקורת
    val reviewText: String = "", // ✅ תוכן הביקורת
    val imageUrl: String? = null, // ✅ אופציונלי - תמונה (למשל כריכת הספר)
    val timestamp: Long = System.currentTimeMillis(), // ✅ תאריך יצירת הביקורת
    val userId: String = "" // ✅ מזהה המשתמש שכתב את הביקורת
)
