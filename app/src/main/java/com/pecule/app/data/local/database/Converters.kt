package com.pecule.app.data.local.database

import androidx.room.TypeConverter
import com.pecule.app.data.local.database.entity.Category
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromCategory(category: Category?): String? {
        return category?.name
    }

    @TypeConverter
    fun toCategory(name: String?): Category? {
        return name?.let { Category.valueOf(it) }
    }
}
