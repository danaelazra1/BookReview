package com.idz.bookreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    var username: String = "",
    val email: String = "",
    var profileImageUrl: String? = null
)
