package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookTitle: String = "",
    val reviewText: String = "",
    val imageUrl: String? = null,
    var isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)
