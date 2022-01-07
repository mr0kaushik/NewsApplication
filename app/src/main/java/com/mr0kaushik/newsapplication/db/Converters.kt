package com.mr0kaushik.newsapplication.db

import androidx.room.TypeConverter
import com.mr0kaushik.newsapplication.data.Source

class Converters {

    @TypeConverter
    fun fromArticleSource(source: Source): String {
        return "${source.id};${source.name}"
    }

    @TypeConverter
    fun toArticleSource(name: String): Source {
        val list = name.split(";")
        return Source(list[0], list[1])
    }
}