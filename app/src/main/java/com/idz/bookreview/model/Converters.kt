package com.idz.bookreview.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromList(list: List<String>?): String {
        return Gson().toJson(list ?: emptyList<String>())
    }

    @TypeConverter
    fun toList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(data, listType)
        }
}