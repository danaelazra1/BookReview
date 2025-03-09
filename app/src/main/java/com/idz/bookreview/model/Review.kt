package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String = "",
    val bookTitle: String = "",
    val reviewText: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)
