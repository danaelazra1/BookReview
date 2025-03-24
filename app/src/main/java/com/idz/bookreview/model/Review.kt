package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String = "",  // מסומן כ-primaryKey אבל לא מנסה לייצר אותו אוטומטית
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val author: String = "",
    val review: String = "",
    var imageUrl: String? = null,
    val timestamp: Long = 0L,
    var isLiked: Boolean = false  // שדה חדש שמייצג האם הפוסט מסומן כאהוב או לא

)
