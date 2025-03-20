package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val bookTitle: String,
    val author: String,
    val reviewText: String,
    val imageUrl: String?,
    val timestamp: Long
)
