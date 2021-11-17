package com.androiddevs.mvvmnewsapp.ui.database

import androidx.room.TypeConverter
import com.androiddevs.mvvmnewsapp.ui.models.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source) : String{
        return source.name;
    }
    @TypeConverter
    fun toSource(name: String) : Source
    {
        return Source(name,name)
    }
}