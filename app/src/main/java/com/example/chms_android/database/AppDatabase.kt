package com.example.chms_android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chms_android.common.Constants
import com.example.chms_android.dao.DoctorDao
import com.example.chms_android.dao.UserDao
import com.example.chms_android.data.Doctor
import com.example.chms_android.data.User
import com.example.chms_android.data.converter.LocalDateTimeConverter
import com.example.chms_android.data.converter.TimestampConverter

@Database(entities = [User::class, Doctor::class], version = Constants.DATABASE_VERSION, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class, TimestampConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun doctorDao(): DoctorDao
}