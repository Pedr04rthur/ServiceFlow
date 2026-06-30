package com.example.serviceflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User

@Database(entities = [OrdemServico::class, User::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ordemDao(): OrdemDao
}
