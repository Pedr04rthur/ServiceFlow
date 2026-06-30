package com.example.serviceflow.data.local

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }

    @TypeConverter
    fun timestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
}
