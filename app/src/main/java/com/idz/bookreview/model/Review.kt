package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val author: String = "",
    val review: String = "",
    var imageUrl: String? = null,
    val timestamp: Long = 0L,
    @TypeConverters(Converters::class)
    var favoritedByUsers: List<String> = emptyList()
) {
    constructor() : this("", "", "", "", "", "", null, 0L, emptyList())
}

